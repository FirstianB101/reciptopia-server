package kr.reciptopia.reciptopiaserver.business.service.spec;

import javax.persistence.criteria.Join;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import org.springframework.data.jpa.domain.Specification;

public class CommentSpecs {

    public static Specification<Comment> isOwner(Long ownerId) {
        return (root, query, builder) -> {
            Join<Comment, Account> join = root.joinSet("owner");
            return builder.equal(join.get("id"), ownerId);
        };
    }

    public static Specification<Comment> isPost(Long postId) {
        return (root, query, builder) -> {
            Join<Comment, Post> join = root.joinSet("post");
            return builder.equal(join.get("id"), postId);
        };
    }

    public static Specification<Comment> hasReply(Long replyId) {
        return (root, query, builder) -> {
            Join<Comment, Reply> join = root.joinSet("replies");
            return builder.equal(join.get("id"), replyId);
        };
    }

    public static Specification<Comment> hasCommentLikeTag(Long commentLikeTagId) {
        return (root, query, builder) -> {
            Join<Comment, CommentLikeTag> join = root.joinSet("commentLikeTags");
            return builder.equal(join.get("id"), commentLikeTagId);
        };
    }
}
