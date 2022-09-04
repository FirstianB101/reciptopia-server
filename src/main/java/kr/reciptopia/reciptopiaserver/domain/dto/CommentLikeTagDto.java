package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.helper.CollectorHelper.noInit;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import lombok.Builder;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface CommentLikeTagDto {

    @With
    @Builder
    record Create(
        @NotNull Long ownerId, @NotNull Long commentId) {

        public CommentLikeTag asEntity(
            Function<? super CommentLikeTag, ? extends CommentLikeTag> initialize) {
            return initialize.apply(CommentLikeTag.builder()
                .build());
        }

        public CommentLikeTag asEntity() {
            return asEntity(noInit());
        }
    }

    @With
    @Builder
    record Result(
        @NotNull Long id, @NotNull Long ownerId, @NotNull Long commentId) {

        public static Result of(CommentLikeTag entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .commentId(entity.getComment().getId())
                .build();
        }

        public static List<Result> of(Streamable<CommentLikeTag> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
