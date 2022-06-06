package kr.reciptopia.reciptopiaserver.business.service.helper;

import javax.persistence.EntityManager;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public record RepositoryHelper(EntityManager em,
                               ServiceErrorHelper errorHelper) {

    public <T> T findOrThrow(Class<T> clazz, Object primaryKey) throws ResponseStatusException {
        T entity = em.find(clazz, primaryKey);
        if (entity == null) {
            String reason = String.format("%s id was not found", clazz.getName());
            throw errorHelper.notFound(reason);
        }
        return entity;
    }

    public Account findAccountOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Account.class, id);
    }

    public AccountProfileImg findAccountProfileImgOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(AccountProfileImg.class, id);
    }

    public Post findPostOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Post.class, id);
    }

    public PostImg findPostImgOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(PostImg.class, id);
    }

    public Comment findCommentOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Comment.class, id);
    }

    public Reply findReplyOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Reply.class, id);
    }

    public PostLikeTag findPostLikeTagOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(PostLikeTag.class, id);
    }

    public CommentLikeTag findCommentLikeTagOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(CommentLikeTag.class, id);
    }

    public ReplyLikeTag findReplyLikeTagOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(ReplyLikeTag.class, id);
    }

    public Recipe findRecipeOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Recipe.class, id);
    }

    public Step findStepOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Step.class, id);
    }

    public MainIngredient findMainIngredientOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(MainIngredient.class, id);
    }

    public SubIngredient findSubIngredientOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(SubIngredient.class, id);
    }

    public SearchHistory findSearchHistoryOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(SearchHistory.class, id);
    }

    public Favorite findFavoriteOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Favorite.class, id);
    }
}
