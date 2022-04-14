package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import lombok.Builder;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface PostLikeTagDto {

    @With
    record Create(
        Long ownerId, Long postId) {

        @Builder
        public Create(
            @NotNull
                Long ownerId,

            @NotNull
                Long postId) {
            this.ownerId = ownerId;
            this.postId = postId;
        }

        public PostLikeTag asEntity(
            Function<? super PostLikeTag, ? extends PostLikeTag> initialize) {
            return initialize.apply(PostLikeTag.builder()
                .build());
        }

        public PostLikeTag asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Result(
        Long id, Long ownerId, Long postId
    ) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long ownerId,

            @NotNull
                Long postId) {
            this.id = id;
            this.ownerId = ownerId;
            this.postId = postId;
        }

        public static Result of(PostLikeTag entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .postId(entity.getPost().getId())
                .build();
        }

        public static List<Result> of(Streamable<PostLikeTag> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
