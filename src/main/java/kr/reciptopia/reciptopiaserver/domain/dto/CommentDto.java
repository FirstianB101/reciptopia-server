package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface CommentDto {

	@Data
	@Builder
	@With
	class Create {

		@NotNull
		private Long ownerId;

		@NotNull
		private Long postId;

		@NotBlank
		@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
		private String content;
	}

	@Data
	@Builder
	@With
	class Update {

		@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
		private String content;

		private Set<Reply> replies;

		private Set<CommentLikeTag> commentLikeTags;
	}

	@Data
	@Builder
	@With
	class Result {

		@NotNull
		private Long id;

		@NotNull
		private Long ownerId;

		@NotNull
		private Long postId;

		@NotBlank
		@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
		private String content;

		private Set<Reply> replies;

		private Set<CommentLikeTag> commentLikeTags;

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
