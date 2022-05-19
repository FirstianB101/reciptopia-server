package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface FavoriteDto {

    interface Bulk {

        @With
        record Result(Map<Long, FavoriteDto.Result> favorites) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, FavoriteDto.Result> favorites) {
                this.favorites = favorites;
            }

            public static Result of(Page<Favorite> favorites) {
                return Result.builder()
                    .favorites(
                        (Map<? extends Long, ? extends FavoriteDto.Result>) favorites.stream()
                            .map(FavoriteDto.Result::of)
                            .collect(
                                Collectors.toMap(
                                    FavoriteDto.Result::id,
                                    result -> result,
                                    (x, y) -> y,
                                    LinkedHashMap::new)))
                    .build();
            }
        }
    }

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

        public static Result of(Favorite favorite) {
            return Result.builder()
                .id(favorite.getId())
                .ownerId(favorite.getOwner().getId())
                .postId(favorite.getPost().getId())
                .build();
        }

        public static List<Result> of(Streamable<Favorite> favorites) {
            return favorites.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
