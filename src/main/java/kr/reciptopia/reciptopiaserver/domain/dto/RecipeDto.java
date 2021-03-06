package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface RecipeDto {

    @With
    record Create(
        Long postId
    ) {

        @Builder
        public Create(@NotNull Long postId) {
            this.postId = postId;
        }

        public Recipe asEntity(
            Function<? super Recipe, ? extends Recipe> initialize) {
            return initialize.apply(Recipe.builder()
                .build());
        }

        public Recipe asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Result(
        Long id, Long postId
    ) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long postId) {
            this.id = id;
            this.postId = postId;
        }

        public static Result of(Recipe recipe) {
            return builder()
                .id(recipe.getId())
                .postId(recipe.getPost().getId())
                .build();
        }

        public static List<Result> of(Streamable<Recipe> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }

    interface Bulk {

        @With
        record Result(
            Map<Long, RecipeDto.Result> recipes) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, RecipeDto.Result> recipes
            ) {
                this.recipes = recipes;
            }

            public static Result of(Collection<Recipe> recipes) {
                return Result.builder()
                    .recipes((Map<? extends Long, ? extends RecipeDto.Result>) recipes.stream()
                        .map(RecipeDto.Result::of)
                        .collect(
                            Collectors.toMap(
                                RecipeDto.Result::id,
                                result -> result,
                                (x, y) -> y,
                                LinkedHashMap::new)))
                    .build();
            }
        }
    }

}
