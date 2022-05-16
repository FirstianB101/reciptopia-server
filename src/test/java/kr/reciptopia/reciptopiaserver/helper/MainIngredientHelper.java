package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create;
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

    public static Create aMainIngredientCreateDto(
        Function<? super Create, ? extends Create> initialize) {
        Create createDto = Create.builder()
            .build();

        createDto = initialize.apply(createDto);
        return Create.builder()
            .recipeId(createDto.recipeId() == null ? ARBITRARY_RECIPE_ID : createDto.recipeId())
            .name(ARBITRARY_NAME)
            .detail(ARBITRARY_DETAIL)
            .build();
    }

    public static Create aMainIngredientCreateDto() {
        return aMainIngredientCreateDto(noInit());
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

        static MainIngredientDto.Bulk.Create tripleMainIngredientsBulkCreateDto(
            Function<? super Create, ? extends Create> initialize) {
            return MainIngredientDto.Bulk.Create.builder()
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateDto(initialize))
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateDto(initialize))
                .mainIngredient(MainIngredientHelper.aMainIngredientCreateDto(initialize))
                .build();
        }

        static MainIngredientDto.Bulk.Create tripleMainIngredientsBulkCreateDto() {
            return tripleMainIngredientsBulkCreateDto(noInit());
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

        static MainIngredientDto.Bulk.Result tripleMainIngredientsBulkResultDto() {
            return MainIngredientDto.Bulk.Result.builder()
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
