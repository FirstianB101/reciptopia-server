package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.Result;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.FavoriteAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.FavoriteSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.FavoriteRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.FavoriteRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final FavoriteRepositoryImpl favoriteRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final FavoriteAuthorizer favoriteAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
        Post post = repoHelper.findPostOrThrow(dto.postId());

        favoriteAuthorizer.requireByOneself(authentication, owner);

        Favorite favorite = dto.asEntity(it->it
            .withOwner(owner)
            .withPost(post));

        return Result.of(favoriteRepository.save(favorite));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findFavoriteOrThrow(id));
    }

    public Bulk.Result search(FavoriteSearchCondition favoriteSearchCondition,
        Pageable pageable) {
        PageImpl<Favorite> pageImpl = favoriteRepositoryImpl.search(favoriteSearchCondition,
            pageable);
        return Bulk.Result.of(pageImpl);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Favorite favorite = repoHelper.findFavoriteOrThrow(id);
        favoriteAuthorizer.requireFavoriteOwner(authentication, favorite);

        favoriteRepository.delete(favorite);
    }
}
