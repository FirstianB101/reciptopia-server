package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import lombok.Builder;
import lombok.With;

public interface CommentDto {

    @With
    record Create(
        Long ownerId, Long postId, String content) {

        @Builder
        public Create(
            @NotNull
                Long ownerId,

            @NotNull
                Long postId,

            @NotBlank
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content) {
            this.ownerId = ownerId;
            this.postId = postId;
            this.content = content;
        }
    }

    @With
    record Update(
        String content, Set<Reply> replies, Set<CommentLikeTag> commentLikeTags) {

        @Builder
        public Update(
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content,

            Set<Reply> replies,

            Set<CommentLikeTag> commentLikeTags) {
            this.content = content;
            this.replies = replies;
            this.commentLikeTags = commentLikeTags;
        }
    }

    @With
    record Result(
        Long id, Long ownerId, Long postId, String content,
        Set<Reply> replies, Set<CommentLikeTag> commentLikeTags) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long ownerId,

            @NotNull
                Long postId,

            @NotBlank
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content,

            Set<Reply> replies,

            Set<CommentLikeTag> commentLikeTags) {
            this.id = id;
            this.ownerId = ownerId;
            this.postId = postId;
            this.content = content;
            this.replies = replies;
            this.commentLikeTags = commentLikeTags;
        }

        public static Result of(Comment entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .postId(entity.getPost().getId())
                .content(entity.getContent())
                .replies(entity.getReplies())
                .commentLikeTags(entity.getCommentLikeTags())
                .build();
        }
    }
}
