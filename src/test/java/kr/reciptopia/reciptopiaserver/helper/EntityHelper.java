package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.CommentHelper.aComment;
import static kr.reciptopia.reciptopiaserver.helper.CommentLikeTagHelper.aCommentLikeTag;
import static kr.reciptopia.reciptopiaserver.helper.FavoriteHelper.aFavorite;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.aMainIngredient;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;
import static kr.reciptopia.reciptopiaserver.helper.PostLikeTagHelper.aPostLikeTag;
import static kr.reciptopia.reciptopiaserver.helper.RecipeHelper.aRecipe;
import static kr.reciptopia.reciptopiaserver.helper.ReplyHelper.aReply;
import static kr.reciptopia.reciptopiaserver.helper.SearchHistoryHelper.aSearchHistory;
import static kr.reciptopia.reciptopiaserver.helper.StepHelper.aStep;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.aSubIngredient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.persistence.EntityManager;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import org.springframework.stereotype.Component;

@Component
public record EntityHelper(EntityManager em) {

    public Account generateAccount() {
        return generateAccount(noInit());
    }

    public Account generateAccount(Function<? super Account, ? extends Account> initialize) {
        Account account = anAccount().withId(null);

        account = initialize.apply(account);
        em.persist(account);

        return account;
    }

    public Post generatePost() {
        return generatePost(noInit());
    }

    public Post generatePost(Function<? super Post, ? extends Post> initialize) {
        Post post = aPost()
            .withId(null)
            .withOwner(null)
            .withPictureUrls(new ArrayList<>());

        post = initialize.apply(post);
        if (post.getOwner() == null) {
            post.setOwner(generateAccount());
        }

        em.persist(post);
        return post;
    }

    public Comment generateComment() {
        return generateComment(noInit());
    }

    public Comment generateComment(Function<? super Comment, ? extends Comment> initialize) {
        Comment comment = aComment()
            .withId(null)
            .withOwner(null)
            .withPost(null);

        comment = initialize.apply(comment);
        if (comment.getOwner() == null) {
            comment.setOwner(generateAccount());
        }
        if (comment.getPost() == null) {
            comment.setPost(generatePost());
        }

        em.persist(comment);
        return comment;
    }

    public Reply generateReply() {
        return generateReply(noInit());
    }

    public Reply generateReply(Function<? super Reply, ? extends Reply> initialize) {
        Reply reply = aReply()
            .withId(null)
            .withOwner(null)
            .withComment(null);

        reply = initialize.apply(reply);
        if (reply.getOwner() == null) {
            reply.setOwner(generateAccount());
        }
        if (reply.getComment() == null) {
            reply.setComment(generateComment());
        }

        em.persist(reply);
        return reply;
    }

    public PostLikeTag generatePostLikeTag() {
        return generatePostLikeTag(noInit());
    }

    public PostLikeTag generatePostLikeTag(
        Function<? super PostLikeTag, ? extends PostLikeTag> initialize) {
        PostLikeTag postLikeTag = aPostLikeTag()
            .withId(null)
            .withOwner(null)
            .withPost(null);

        postLikeTag = initialize.apply(postLikeTag);
        if (postLikeTag.getOwner() == null) {
            postLikeTag.setOwner(generateAccount());
        }
        if (postLikeTag.getPost() == null) {
            postLikeTag.setPost(generatePost());
        }

        em.persist(postLikeTag);
        return postLikeTag;
    }

    public CommentLikeTag generateCommentLikeTag() {
        return generateCommentLikeTag(noInit());
    }

    public CommentLikeTag generateCommentLikeTag(
        Function<? super CommentLikeTag, ? extends CommentLikeTag> initialize) {
        CommentLikeTag commentLikeTag = aCommentLikeTag()
            .withId(null)
            .withOwner(null)
            .withComment(null);

        commentLikeTag = initialize.apply(commentLikeTag);
        if (commentLikeTag.getOwner() == null) {
            commentLikeTag.setOwner(generateAccount());
        }
        if (commentLikeTag.getComment() == null) {
            commentLikeTag.setComment(generateComment());
        }

        em.persist(commentLikeTag);
        return commentLikeTag;
    }

    public Recipe generateRecipe() {
        return generateRecipe(noInit());
    }

    public Recipe generateRecipe(Function<? super Recipe, ? extends Recipe> initialize) {
        Recipe recipe = aRecipe()
            .withId(null)
            .withPost(null);

        recipe = initialize.apply(recipe);
        if (recipe.getPost() == null) {
            recipe.setPost(generatePost());
        }

        em.persist(recipe);

        return recipe;
    }

    public MainIngredient generateMainIngredient() {
        return generateMainIngredient(noInit());
    }

    public MainIngredient generateMainIngredient(
        Function<? super MainIngredient, ? extends MainIngredient> initialize) {
        MainIngredient mainIngredient = aMainIngredient()
            .withId(null)
            .withRecipe(null);

        mainIngredient = initialize.apply(mainIngredient);
        if (mainIngredient.getRecipe() == null) {
            mainIngredient.setRecipe(generateRecipe());
        }
        em.persist(mainIngredient);

        return mainIngredient;
    }

    public SubIngredient generateSubIngredient() {
        return generateSubIngredient(noInit());
    }

    public SubIngredient generateSubIngredient(
        Function<? super SubIngredient, ? extends SubIngredient> initialize) {
        SubIngredient subIngredient = aSubIngredient()
            .withId(null)
            .withRecipe(null);

        subIngredient = initialize.apply(subIngredient);
        if (subIngredient.getRecipe() == null) {
            subIngredient.setRecipe(generateRecipe());
        }
        em.persist(subIngredient);

        return subIngredient;
    }

    public Step generateStep(Function<? super Step, ? extends Step> initialize) {
        Step step = aStep()
            .withId(null)
            .withRecipe(null);

        step = initialize.apply(step);
        if (step.getRecipe() == null) {
            step.setRecipe(generateRecipe());
        }
        em.persist(step);

        return step;
    }

    public Step generateStep() {
        return generateStep(noInit());
    }

    private <T> Function<? super T, ? extends T> noInit() {
        return (arg) -> arg;
    }

    public SearchHistory generateSearchHistory() {
        return generateSearchHistory(noInit());
    }

    public SearchHistory generateSearchHistory(
        Function<? super SearchHistory, ? extends SearchHistory> initialize) {
        SearchHistory searchHistory = aSearchHistory()
            .withId(null)
            .withOwner(null)
            .withIngredientNames(new HashSet<>());

        searchHistory = initialize.apply(searchHistory);
        if (searchHistory.getOwner() == null) {
            searchHistory.setOwner(generateAccount());
        }
        if (searchHistory.getIngredientNames().isEmpty()) {
            searchHistory.setIngredientNames(Set.of("된장", "감자", "두부"));
        }

        em.persist(searchHistory);

        return searchHistory;
    }

    public Favorite generateFavorite() {
        return generateFavorite(noInit());
    }

    public Favorite generateFavorite(Function<? super Favorite, ? extends Favorite> initialize) {
        Favorite favorite = aFavorite()
            .withId(null)
            .withOwner(null)
            .withPost(null);

        favorite = initialize.apply(favorite);
        if (favorite.getOwner() == null) {
            favorite.setOwner(generateAccount());
        }
        if(favorite.getPost() == null){
            favorite.setPost(generatePost());
        }

        em.persist(favorite);

        return favorite;
    }

}
