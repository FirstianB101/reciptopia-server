package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.RecipePostDto.Create;
import static kr.reciptopia.reciptopiaserver.helper.MainIngredientHelper.Bulk.tripleMainIngredientsBulkCreateWithRecipeDto;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPostCreateDto;
import static kr.reciptopia.reciptopiaserver.helper.StepHelper.Bulk.tripleStepsBulkCreateWithRecipeDto;
import static kr.reciptopia.reciptopiaserver.helper.SubIngredientHelper.Bulk.tripleSubIngredientsBulkCreateWithRecipeDto;

import kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto;
import kr.reciptopia.reciptopiaserver.domain.dto.StepDto;
import kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto;

public class RecipePostHelper {


    private static final PostDto.Create
        ARBITRARY_POST = aPostCreateDto();
    private static final MainIngredientDto.Bulk.Create.WithRecipe
        ARBITRARY_MAIN_INGREDIENTS = tripleMainIngredientsBulkCreateWithRecipeDto();
    private static final SubIngredientDto.Bulk.Create.WithRecipe
        ARBITRARY_SUB_INGREDIENTS = tripleSubIngredientsBulkCreateWithRecipeDto();
    private static final StepDto.Bulk.Create.WithRecipe
        ARBITRARY_STEPS = tripleStepsBulkCreateWithRecipeDto();


    public static Create aRecipePostCreateDto() {
        return Create.builder()
            .post(ARBITRARY_POST)
            .mainIngredients(ARBITRARY_MAIN_INGREDIENTS)
            .subIngredients(ARBITRARY_SUB_INGREDIENTS)
            .steps(ARBITRARY_STEPS)
            .build();
    }

}
