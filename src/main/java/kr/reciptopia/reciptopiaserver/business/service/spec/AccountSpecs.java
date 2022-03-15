package kr.reciptopia.reciptopiaserver.business.service.spec;


import javax.persistence.criteria.Join;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecs {

    public static Specification<Account> hasPost(Long postId) {
        return (root, query, builder) -> {
            Join<Account, Post> join = root.joinSet("posts");
            return builder.equal(join.get("id"), postId);
        };
    }

    public static Specification<Account> hasComment(Long commentId) {
        return (root, query, builder) -> {
            Join<Account, Comment> join = root.joinSet("comments");
            return builder.equal(join.get("id"), commentId);
        };
    }

    public static Specification<Account> hasReply(Long replyId) {
        return (root, query, builder) -> {
            Join<Account, Reply> join = root.joinSet("replies");
            return builder.equal(join.get("id"), replyId);
        };
    }
}
