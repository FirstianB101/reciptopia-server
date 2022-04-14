package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.*;

import kr.reciptopia.reciptopiaserver.business.service.authorizer.FavoriteAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
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

    public List<Result> search(Pageable pageable) {
        Page<Favorite> favorites = favoriteRepository.findAll(pageable);
        return Result.of(favorites);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Favorite favorite = repoHelper.findFavoriteOrThrow(id);
        favoriteAuthorizer.requireFavoriteOwner(authentication, favorite);

        favoriteRepository.delete(favorite);
    }
}
