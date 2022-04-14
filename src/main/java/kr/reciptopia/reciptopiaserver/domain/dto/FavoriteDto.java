package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import lombok.Builder;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface FavoriteDto {

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

        public Favorite asEntity(
            Function<? super Favorite, ? extends Favorite> initialize) {
            return initialize.apply(Favorite.builder()
                .build());
        }

        public Favorite asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Result(
        Long id, Long ownerId, Long postId) {

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

        public static Result of(Favorite entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .postId(entity.getPost().getId())
                .build();
        }

        public static List<Result> of(Streamable<Favorite> favorites) {
            return favorites.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
