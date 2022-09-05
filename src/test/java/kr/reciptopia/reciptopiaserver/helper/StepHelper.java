package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Create.WithRecipe;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Update;
import static kr.reciptopia.reciptopiaserver.domain.dto.helper.InitializationHelper.noInit;
import static kr.reciptopia.reciptopiaserver.helper.RecipeHelper.aRecipe;

import java.util.function.Function;
import kr.reciptopia.reciptopiaserver.domain.dto.StepDto;
import kr.reciptopia.reciptopiaserver.domain.model.Step;

public class StepHelper {

    private static final String ARBITRARY_DESCRIPTION =
        "고춧가루 2수저 가득, 고추장 1수저 가득, "
            + "양조간장 3수저, 맛술 1수저, 설탕 2수저, "
            + "올리고당 1수저, 다진 마늘 1수저 넣고 양념장을 만든다.";
    private static final Long ARBITRARY_ID = 0L;
    private static final Long ARBITRARY_RECIPE_ID = 0L;
    private static final Long ARBITRARY_BULK_ID_0 = 0L;
    private static final Long ARBITRARY_BULK_ID_1 = 1L;
    private static final Long ARBITRARY_BULK_ID_2 = 2L;

    public static Step aStep() {
        return Step.builder()
            .recipe(aRecipe().withId(ARBITRARY_RECIPE_ID))
            .description(ARBITRARY_DESCRIPTION)
            .build()
            .withId(ARBITRARY_RECIPE_ID);
    }

    public static Single aStepCreateDto(Function<? super Single, ? extends Single> initialize) {
        Single singleDto = Single.builder()
            .build();

        singleDto = initialize.apply(singleDto);
        return Single.builder()
            .recipeId(singleDto.recipeId() == null ? ARBITRARY_RECIPE_ID : singleDto.recipeId())
            .description(ARBITRARY_DESCRIPTION)
            .build();
    }

    public static Single aStepCreateDto() {
        return aStepCreateDto(noInit());
    }

    public static WithRecipe aStepCreateWithRecipeDto() {
        return WithRecipe.builder()
            .description(ARBITRARY_DESCRIPTION)
            .build();
    }

    public static Update aStepUpdateDto() {
        return Update.builder()
            .description(ARBITRARY_DESCRIPTION)
            .build();
    }

    public static Result aStepResultDto() {
        return Result.builder()
            .id(ARBITRARY_ID)
            .recipeId(ARBITRARY_RECIPE_ID)
            .description(ARBITRARY_DESCRIPTION)
            .build();
    }

    public interface Bulk {

        static StepDto.Bulk.Create.Single tripleStepsBulkCreateDto(
            Function<? super Single, ? extends Single> initialize) {
            return StepDto.Bulk.Create.Single.builder()
                .step(StepHelper.aStepCreateDto(initialize))
                .step(StepHelper.aStepCreateDto(initialize))
                .step(StepHelper.aStepCreateDto(initialize))
                .build();
        }

        static StepDto.Bulk.Create.Single tripleStepsBulkCreateDto() {
            return tripleStepsBulkCreateDto(noInit());
        }

        static StepDto.Bulk.Create.WithRecipe tripleStepsBulkCreateWithRecipeDto() {
            return StepDto.Bulk.Create.WithRecipe.builder()
                .step(StepHelper.aStepCreateWithRecipeDto())
                .step(StepHelper.aStepCreateWithRecipeDto())
                .step(StepHelper.aStepCreateWithRecipeDto())
                .build();
        }

        static StepDto.Bulk.Update tripleStepsBulkUpdateDto() {
            return StepDto.Bulk.Update.builder()
                .step(ARBITRARY_BULK_ID_0, StepHelper.aStepUpdateDto())
                .step(ARBITRARY_BULK_ID_1, StepHelper.aStepUpdateDto())
                .step(ARBITRARY_BULK_ID_2, StepHelper.aStepUpdateDto())
                .build();
        }

        static StepDto.Bulk.Result tripleStepsBulkResultDto() {
            return StepDto.Bulk.Result.builder()
                .step(ARBITRARY_BULK_ID_0, StepHelper.aStepResultDto())
                .step(ARBITRARY_BULK_ID_1, StepHelper.aStepResultDto())
                .step(ARBITRARY_BULK_ID_2, StepHelper.aStepResultDto())
                .build();
        }
    }
}
