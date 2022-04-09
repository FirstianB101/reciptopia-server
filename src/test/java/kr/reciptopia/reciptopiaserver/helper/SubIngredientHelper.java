package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.RecipeHelper.aRecipe;

import kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;

public class SubIngredientHelper {

    private static final String ARBITRARY_NAME = "간장";
    private static final String ARBITRARY_DETAIL = "한 큰술";
    private static final Long ARBITRARY_ID = 0L;
    private static final Long ARBITRARY_RECIPE_ID = 1L;
    private static final Long ARBITRARY_BULK_ID_0 = 0L;
    private static final Long ARBITRARY_BULK_ID_1 = 1L;
    private static final Long ARBITRARY_BULK_ID_2 = 2L;

    public static SubIngredient aSubIngredient() {
        return SubIngredient.builder()
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .recipe(aRecipe().withId(ARBITRARY_RECIPE_ID))
            .build()
            .withId(ARBITRARY_ID);
    }

    public static Create aSubIngredientCreateDto() {
        return Create.builder()
            .recipeId(ARBITRARY_RECIPE_ID)
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public static Update aSubIngredientUpdateDto() {
        return Update.builder()
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public static Result aSubIngredientResultDto() {
        return Result.builder()
            .id(ARBITRARY_ID)
            .recipeId(ARBITRARY_RECIPE_ID)
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public interface Bulk {

        static SubIngredientDto.Bulk.Create aSubIngredientCreateDto() {
            return SubIngredientDto.Bulk.Create.builder()
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto())
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto())
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto())
                .build();
        }

        static SubIngredientDto.Bulk.Update aSubIngredientUpdateDto() {
            return SubIngredientDto.Bulk.Update.builder()
                .subIngredient(ARBITRARY_BULK_ID_0,
                    SubIngredientHelper.aSubIngredientUpdateDto())
                .subIngredient(ARBITRARY_BULK_ID_1,
                    SubIngredientHelper.aSubIngredientUpdateDto())
                .subIngredient(ARBITRARY_BULK_ID_2,
                    SubIngredientHelper.aSubIngredientUpdateDto())
                .build();
        }

        static SubIngredientDto.Bulk.Result aSubIngredientResultDto() {
            return SubIngredientDto.Bulk.Result.builder()
                .subIngredient(ARBITRARY_BULK_ID_0,
                    SubIngredientHelper.aSubIngredientResultDto())
                .subIngredient(ARBITRARY_BULK_ID_1,
                    SubIngredientHelper.aSubIngredientResultDto())
                .subIngredient(ARBITRARY_BULK_ID_2,
                    SubIngredientHelper.aSubIngredientResultDto())
                .build();
        }
    }
}
