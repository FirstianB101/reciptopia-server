package kr.reciptopia.reciptopiaserver.business.service;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.PostAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostSearchCondition;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.RecipeSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostLikeTagRepository;
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
    private final CommentRepository commentRepository;
    private final PostLikeTagRepository postLikeTagRepository;
    private final PostRepositoryImpl postRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final PostAuthorizer postAuthorizer;
    private final RecipeService recipeService;
    private final ServiceErrorHelper errorHelper;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Post post = dto.asEntity();

        Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
        postAuthorizer.requireByOneself(authentication, owner);

        post.setOwner(owner);
        return Result.of(postRepository.save(post));
    }

    @Transactional
    public Result read(Long id) {
        Post post = repoHelper.findPostOrThrow(id);
        post.addViews();
        return Result.of(postRepository.save(post));
    }

    public Bulk.ResultWithCommentAndLikeTagCount search(PostSearchCondition condition,
        Pageable pageable) {
        RecipeSearchCondition recipeSearchCondition = condition.getRecipeSearchCondition();
        List<Long> postIds = getPostIdsFromRecipeCondition(pageable, recipeSearchCondition);
        PageImpl<Post> posts = postRepositoryImpl.search(
            condition.updateConditionWithRecipeCondition(recipeSearchCondition, postIds),
            pageable);

        return Bulk.ResultWithCommentAndLikeTagCount.of(
            posts,
            commentRepository::countByPostId,
            postLikeTagRepository::countByPostId);
    }

    private List<Long> getPostIdsFromRecipeCondition(Pageable pageable,
        RecipeSearchCondition recipeSearchCondition) {
        RecipeDto.Bulk.Result search = recipeService.search(recipeSearchCondition, pageable);
        return search
            .recipes().values()
            .stream().map(RecipeDto.Result::postId).toList();
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Post post = repoHelper.findPostOrThrow(id);
        postAuthorizer.requirePostOwner(authentication, post);

        if (dto.pictureUrls() != null) {
            post.setPictureUrls(dto.pictureUrls());
        }
        if (dto.title() != null) {
            throwExceptionWhenBlankTitle(dto.title());
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

    public void throwExceptionWhenBlankTitle(String title) {
        if (title.isBlank()) {
            throw errorHelper.badRequest(
                "Title must not be null and must contain at least one non-whitespace character");
        }
    }
}
