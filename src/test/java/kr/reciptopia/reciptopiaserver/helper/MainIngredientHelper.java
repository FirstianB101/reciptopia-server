package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Update;
import static kr.reciptopia.reciptopiaserver.helper.RecipeHelper.aRecipe;

import java.util.function.Function;
import kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;

public class MainIngredientHelper {

    private static final String ARBITRARY_NAME = "청경채";
    private static final String ARBITRARY_DETAIL = "한 묶음";
    private static final Long ARBITRARY_ID = 0L;
    private static final Long ARBITRARY_RECIPE_ID = 1L;
    private static final Long ARBITRARY_BULK_ID_0 = 0L;
    private static final Long ARBITRARY_BULK_ID_1 = 1L;
    private static final Long ARBITRARY_BULK_ID_2 = 2L;

    public static MainIngredient aMainIngredient() {
        return MainIngredient.builder()
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .recipe(aRecipe().withId(ARBITRARY_RECIPE_ID))
            .build()
            .withId(ARBITRARY_ID);
    }

    public static Single aMainIngredientCreateDto(
        Function<? super Single, ? extends Single> initialize) {
        Single singleDto = MainIngredientDto.Create.Single.builder()
            .build();

        singleDto = initialize.apply(singleDto);
        return MainIngredientDto.Create.Single.builder()
            .recipeId(singleDto.recipeId() == null ? ARBITRARY_RECIPE_ID : singleDto.recipeId())
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public static Single aMainIngredientCreateDto() {
        return aMainIngredientCreateDto(noInit());
    }

    public static MainIngredientDto.Create.WithRecipe aMainIngredientCreateWithRecipeDto() {
        return MainIngredientDto.Create.WithRecipe.builder()
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public static Update aMainIngredientUpdateDto() {
        return Update.builder()
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public static Result aMainIngredientResultDto() {
        return Result.builder()
            .id(ARBITRARY_ID)
            .recipeId(ARBITRARY_RECIPE_ID)
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    private static <T> Function<? super T, ? extends T> noInit() {
        return (arg) -> arg;
    }

    public interface Bulk {

        static MainIngredientDto.Bulk.Create.Single tripleMainIngredientsBulkCreateDto(
            Function<? super Single, ? extends Single> initialize) {
            return MainIngredientDto.Bulk.Create.Single.builder()
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateDto(initialize))
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateDto(initialize))
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateDto(initialize))
                .build();
        }

        static MainIngredientDto.Bulk.Create.Single tripleMainIngredientsBulkCreateDto() {
            return tripleMainIngredientsBulkCreateDto(noInit());
        }

        static MainIngredientDto.Bulk.Create.WithRecipe tripleMainIngredientsBulkCreateWithRecipeDto() {
            return MainIngredientDto.Bulk.Create.WithRecipe.builder()
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateWithRecipeDto())
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateWithRecipeDto())
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateWithRecipeDto())
                .build();
        }

        static MainIngredientDto.Bulk.Update tripleMainIngredientsBulkUpdateDto() {
            return MainIngredientDto.Bulk.Update.builder()
                .mainIngredient(ARBITRARY_BULK_ID_0,
                    MainIngredientHelper.aMainIngredientUpdateDto())
                .mainIngredient(ARBITRARY_BULK_ID_1,
                    MainIngredientHelper.aMainIngredientUpdateDto())
                .mainIngredient(ARBITRARY_BULK_ID_2,
                    MainIngredientHelper.aMainIngredientUpdateDto())
                .build();
        }

        static MainIngredientDto.Bulk.ResultGroupBy.Id tripleMainIngredientsBulkResultDto() {
            return MainIngredientDto.Bulk.ResultGroupBy.Id.builder()
                .mainIngredient(ARBITRARY_BULK_ID_0,
                    MainIngredientHelper.aMainIngredientResultDto())
                .mainIngredient(ARBITRARY_BULK_ID_1,
                    MainIngredientHelper.aMainIngredientResultDto())
                .mainIngredient(ARBITRARY_BULK_ID_2,
                    MainIngredientHelper.aMainIngredientResultDto())
                .build();
        }
    }
}
