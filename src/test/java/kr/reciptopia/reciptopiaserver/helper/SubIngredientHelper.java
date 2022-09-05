package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;
import static kr.reciptopia.reciptopiaserver.domain.dto.helper.CollectorHelper.noInit;
import static kr.reciptopia.reciptopiaserver.helper.RecipeHelper.aRecipe;

import java.util.function.Function;
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

    public static Single aSubIngredientCreateDto(
        Function<? super Single, ? extends Single> initialize) {
        Single singleDto = SubIngredientDto.Create.Single.builder()
            .build();

        singleDto = initialize.apply(singleDto);
        return SubIngredientDto.Create.Single.builder()
            .recipeId(singleDto.recipeId() == null ? ARBITRARY_RECIPE_ID : singleDto.recipeId())
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public static Single aSubIngredientCreateDto() {
        return aSubIngredientCreateDto(noInit());
    }

    public static SubIngredientDto.Create.WithRecipe aSubIngredientCreateWithRecipeDto() {
        return SubIngredientDto.Create.WithRecipe.builder()
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

        static SubIngredientDto.Bulk.Create.Single tripleSubIngredientsBulkCreateDto(
            Function<? super Single, ? extends Single> initialize) {
            return SubIngredientDto.Bulk.Create.Single.builder()
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto(initialize))
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto(initialize))
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto(initialize))
                .build();
        }

        static SubIngredientDto.Bulk.Create.Single tripleSubIngredientsBulkCreateDto() {
            return tripleSubIngredientsBulkCreateDto(noInit());
        }

        static SubIngredientDto.Bulk.Create.WithRecipe tripleSubIngredientsBulkCreateWithRecipeDto() {
            return SubIngredientDto.Bulk.Create.WithRecipe.builder()
                .subIngredient(SubIngredientHelper.aSubIngredientCreateWithRecipeDto())
                .subIngredient(SubIngredientHelper.aSubIngredientCreateWithRecipeDto())
                .subIngredient(SubIngredientHelper.aSubIngredientCreateWithRecipeDto())
                .build();
        }

        static SubIngredientDto.Bulk.Update tripleSubIngredientsBulkUpdateDto() {
            return SubIngredientDto.Bulk.Update.builder()
                .subIngredient(ARBITRARY_BULK_ID_0,
                    SubIngredientHelper.aSubIngredientUpdateDto())
                .subIngredient(ARBITRARY_BULK_ID_1,
                    SubIngredientHelper.aSubIngredientUpdateDto())
                .subIngredient(ARBITRARY_BULK_ID_2,
                    SubIngredientHelper.aSubIngredientUpdateDto())
                .build();
        }

        static SubIngredientDto.Bulk.Result tripleSubIngredientsBulkResultDto() {
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
