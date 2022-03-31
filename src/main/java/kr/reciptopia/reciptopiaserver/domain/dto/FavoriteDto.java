package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import lombok.Builder;
import lombok.With;

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
    }
}
