package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;
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

    public static Create aSubIngredientCreateDto(
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

    public static Create aSubIngredientCreateDto() {
        return aSubIngredientCreateDto(noInit());
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

    private static <T> Function<? super T, ? extends T> noInit() {
        return (arg) -> arg;
    }

    public interface Bulk {

        static SubIngredientDto.Bulk.Create tripleSubIngredientsBulkCreateDto(
            Function<? super Create, ? extends Create> initialize) {
            return SubIngredientDto.Bulk.Create.builder()
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto(initialize))
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto(initialize))
                .subIngredient(SubIngredientHelper.aSubIngredientCreateDto(initialize))
                .build();
        }

        static SubIngredientDto.Bulk.Create tripleSubIngredientsBulkCreateDto() {
            return tripleSubIngredientsBulkCreateDto(noInit());
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
