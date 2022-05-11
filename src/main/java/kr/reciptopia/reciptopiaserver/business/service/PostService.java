package kr.reciptopia.reciptopiaserver.business.service;

import kr.reciptopia.reciptopiaserver.business.service.authorizer.PostAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.PostSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.PostRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostRepositoryImpl postRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final PostAuthorizer postAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Post post = dto.asEntity();

        Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
        postAuthorizer.requireByOneself(authentication, owner);

        post.setOwner(owner);
        return Result.of(postRepository.save(post));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findPostOrThrow(id));
    }

    public Bulk.Result search(PostSearchCondition postSearchCondition, Pageable pageable) {
        PageImpl<Post> pageImpl = postRepositoryImpl.search(postSearchCondition, pageable);
        return Bulk.Result.of(pageImpl);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Post post = repoHelper.findPostOrThrow(id);
        postAuthorizer.requirePostOwner(authentication, post);

        if (dto.pictureUrls() != null) {
            post.setPictureUrls(dto.pictureUrls());
        }
        if (dto.title() != null && !dto.title().isBlank()) {
            post.setTitle(dto.title());
        }
        if (dto.content() != null) {
            post.setContent(dto.content());
        }

        return Result.of(postRepository.save(post));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Post post = repoHelper.findPostOrThrow(id);
        postAuthorizer.requirePostOwner(authentication, post);

        postRepository.delete(post);
    }
}
