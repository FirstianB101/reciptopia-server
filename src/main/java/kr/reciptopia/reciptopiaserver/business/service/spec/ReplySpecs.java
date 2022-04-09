package kr.reciptopia.reciptopiaserver.business.service.spec;

import javax.persistence.criteria.Join;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;
import org.springframework.data.jpa.domain.Specification;

public class ReplySpecs {

    public static Specification<Reply> isOwner(Long ownerId) {
        return (root, query, builder) -> {
            Join<Reply, Account> join = root.joinSet("owner");
            return builder.equal(join.get("id"), ownerId);
        };
    }

    public static Specification<Reply> isComment(Long commentId) {
        return (root, query, builder) -> {
            Join<Reply, Comment> join = root.joinSet("comment");
            return builder.equal(join.get("id"), commentId);
        };
    }

    public static Specification<Reply> hasReplyLikeTag(Long replyLikeTagId) {
        return (root, query, builder) -> {
            Join<Reply, ReplyLikeTag> join = root.joinSet("replyLikeTags");
            return builder.equal(join.get("id"), replyLikeTagId);
        };
    }
}
