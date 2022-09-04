package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.helper.CollectorHelper.noInit;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;
import lombok.Builder;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface ReplyLikeTagDto {

	@With
	record Create(
		Long ownerId, Long replyId) {

		@Builder
		public Create(
			@NotNull
				Long ownerId,

			@NotNull
				Long replyId) {
			this.ownerId = ownerId;
			this.replyId = replyId;
		}

		public ReplyLikeTag asEntity(
			Function<? super ReplyLikeTag, ? extends ReplyLikeTag> initialize) {
			return initialize.apply(ReplyLikeTag.builder().build());
		}

		public ReplyLikeTag asEntity() {
			return asEntity(noInit());
		}
	}

	@With
	record Result(
		Long id, Long ownerId, Long replyId) {

		@Builder
		public Result(
			@NotNull
				Long id,

			@NotNull
				Long ownerId,

			@NotNull
				Long replyId) {
			this.id = id;
			this.ownerId = ownerId;
			this.replyId = replyId;
		}

		public static Result of(ReplyLikeTag entity) {
			return Result.builder()
				.id(entity.getId())
				.ownerId(entity.getOwner().getId())
				.replyId(entity.getReply().getId())
				.build();
		}

		public static List<Result> of(Streamable<ReplyLikeTag> entities) {
			return entities.stream()
				.map(Result::of)
				.collect(Collectors.toList());
		}
	}
}