package kr.reciptopia.reciptopiaserver.business.service.helper;

import javax.persistence.EntityManager;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
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

    public Post findPostOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Post.class, id);
    }

    public Comment findCommentOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Comment.class, id);
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
}
