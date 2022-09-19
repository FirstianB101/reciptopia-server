package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.helper.InitializationHelper.noInit;

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
    @Builder
    record Create(
        @NotNull Long ownerId,
        @NotNull Long replyId) {

        public ReplyLikeTag asEntity(
            Function<? super ReplyLikeTag, ? extends ReplyLikeTag> initialize) {
            return initialize.apply(ReplyLikeTag.builder().build());
        }

        public ReplyLikeTag asEntity() {
            return asEntity(noInit());
        }
    }

	@With
    @Builder
    record Result(
        @NotNull Long id,
        @NotNull Long ownerId,
        @NotNull Long replyId) {

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