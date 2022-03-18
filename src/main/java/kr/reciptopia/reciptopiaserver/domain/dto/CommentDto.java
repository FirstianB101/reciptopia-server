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
		@NotNull
		Long ownerId,

		@NotNull
		Long postId,

		@NotBlank
		@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
		String content
	) {

		@Builder
		public Create {
		}
	}

	@With
	record Update(
		@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
		String content,

		Set<Reply> replies,

		Set<CommentLikeTag> commentLikeTags
	) {

		@Builder
		public Update {
		}
	}

	@With
	record Result(
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

		Set<CommentLikeTag> commentLikeTags
	) {

		@Builder
		public Result {
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
