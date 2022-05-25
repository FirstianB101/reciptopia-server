package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;

public interface RecipePostDto {

    @With
    record Create(
        PostDto.Create post, MainIngredientDto.Bulk.Create.WithRecipe mainIngredients,
        SubIngredientDto.Bulk.Create.WithRecipe subIngredients, StepDto.Bulk.Create.WithRecipe steps
    ) {

        @Builder
        public Create(
            @NotNull
                PostDto.Create post,

            MainIngredientDto.Bulk.Create.WithRecipe mainIngredients,
            SubIngredientDto.Bulk.Create.WithRecipe subIngredients,
            StepDto.Bulk.Create.WithRecipe steps) {
            this.post = post;
            this.mainIngredients = mainIngredients;
            this.subIngredients = subIngredients;
            this.steps = steps;
        }
    }

    @With
    record Result(
        PostDto.Result post, RecipeDto.Result recipe,
        MainIngredientDto.Bulk.ResultGroupBy.Id bulkMainIngredient,
        SubIngredientDto.Bulk.Result bulkSubIngredient, StepDto.Bulk.Result bulkStep
    ) {

        @Builder
        public Result {
        }
    }
}
