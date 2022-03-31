package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Set;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import lombok.Builder;
import lombok.Singular;
import lombok.With;

public interface SearchHistoryDto {

    @With
    record Create(
        Long ownerId, Set<String> ingredients, String recipeName) {

        @Builder
        public Create(
            @NotNull
                Long ownerId,

            @Singular
                Set<String> ingredients,

            String recipeName) {
            this.ownerId = ownerId;
            this.ingredients = ingredients;
            this.recipeName = recipeName;
        }
    }

    @With
    record Result(
        Long id, Long ownerId, Set<String> ingredients, String recipeName) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long ownerId,

            @Singular
                Set<String> ingredients,

            String recipeName) {
            this.id = id;
            this.ownerId = ownerId;
            this.ingredients = ingredients;
            this.recipeName = recipeName;
        }

        public static Result of(SearchHistory entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .ingredients(entity.getIngredients())
                .recipeName(entity.getRecipeName())
                .build();
        }
    }
}
