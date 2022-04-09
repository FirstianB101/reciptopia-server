package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.CommentHelper.aComment;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;
import static kr.reciptopia.reciptopiaserver.helper.ReplyHelper.aReply;
import java.util.ArrayList;
import java.util.function.Function;
import javax.persistence.EntityManager;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
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
            .withRecipe(null)
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

    private <T> Function<? super T, ? extends T> noInit() {
        return (arg) -> arg;
    }

}
