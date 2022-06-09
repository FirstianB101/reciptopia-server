package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Bulk;

import kr.reciptopia.reciptopiaserver.business.service.authorizer.LikeTagAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostLikeTagSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostLikeTagRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.PostLikeTagRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeTagService {

    private final PostLikeTagRepository postLikeTagRepository;
    private final RepositoryHelper repoHelper;
    private final LikeTagAuthorizer likeTagAuthorizer;
    private final PostLikeTagRepositoryImpl postLikeTagRepositoryImpl;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
        Post post = repoHelper.findPostOrThrow(dto.postId());
        likeTagAuthorizer.requireByOneself(authentication, owner);

        PostLikeTag postLikeTag = dto.asEntity(it -> it
            .withOwner(owner)
            .withPost(post)
        );

        return Result.of(postLikeTagRepository.save(postLikeTag));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findPostLikeTagOrThrow(id));
    }

    public Bulk.Result search(PostLikeTagSearchCondition condition, Pageable pageable) {
        Page<PostLikeTag> entities = postLikeTagRepositoryImpl.search(condition, pageable);
        return Bulk.Result.GroupBy.OwnerId.of(entities);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        PostLikeTag postLikeTag = repoHelper.findPostLikeTagOrThrow(id);
        likeTagAuthorizer.requireLikeTagOwner(authentication, postLikeTag);

        postLikeTagRepository.delete(postLikeTag);
    }
}
