package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface StepDto {

    interface Bulk {

        interface Create {

            record WithRecipe(
                List<StepDto.Create.WithRecipe> steps
            ) {

                @Builder
                public WithRecipe(
                    @NotEmpty
                    @Singular
                        List<StepDto.Create.WithRecipe> steps
                ) {
                    this.steps = steps;
                }

                public StepDto.Bulk.Create.Single asSingleDto(
                    Function<? super StepDto.Create.Single, ? extends StepDto.Create.Single> initialize) {
                    List<StepDto.Create.Single> singleDtos = this.steps.stream()
                        .map(m -> m.asSingleDto(initialize))
                        .collect(Collectors.toList());
                    return StepDto.Bulk.Create.Single.builder()
                        .steps(singleDtos)
                        .build();
                }
            }

            @With
            record Single(
                List<StepDto.Create.Single> steps
            ) {

                @Builder
                public Single(
                    @NotEmpty
                    @Singular
                        List<StepDto.Create.Single> steps
                ) {
                    this.steps = steps;
                }

                public List<Step> asEntity() {
                    return this.steps.stream()
                        .map(StepDto.Create.Single::asEntity)
                        .collect(Collectors.toList());
                }
            }
        }

        @With
        record Update(
            Map<Long, StepDto.Update> steps
        ) {

            @Builder
            public Update(
                @NotEmpty
                @Singular
                    Map<Long, StepDto.Update> steps
            ) {
                this.steps = steps;
            }

        }

        @With
        record Result(
            Map<Long, StepDto.Result> steps
        ) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, StepDto.Result> steps
            ) {
                this.steps = steps;
            }

            public static Result of(Page<Step> steps) {
                return Result.of(
                    steps.stream()
                        .map(StepDto.Result::of)
                        .collect(Collectors.toList()));
            }

            public static <T extends Collection<StepDto.Result>> Result of(T results) {
                return Result.builder()
                    .steps((Map<? extends Long, ? extends StepDto.Result>) results.stream()
                        .collect(Collectors.toMap(StepDto.Result::id,
                            result -> result,
                            (x, y) -> y,
                            LinkedHashMap::new)))
                    .build();
            }
        }
    }

    interface Create {

        record WithRecipe(
            String description, String pictureUrl
        ) {

            @Builder
            public WithRecipe(

                @NotEmpty
                    String description,

                String pictureUrl) {
                this.description = description;
                this.pictureUrl = pictureUrl;
            }

            public StepDto.Create.Single asSingleDto(
                Function<? super StepDto.Create.Single, ? extends StepDto.Create.Single> initialize) {
                return initialize.apply(StepDto.Create.Single.builder()
                    .description(description)
                    .pictureUrl(pictureUrl)
                    .build());
            }
        }

        @With
        record Single(
            Long recipeId, String description, String pictureUrl
        ) {

            @Builder
            public Single(
                @NotNull
                    Long recipeId,

                @NotEmpty
                    String description,

                String pictureUrl) {
                this.recipeId = recipeId;
                this.description = description;
                this.pictureUrl = pictureUrl;
            }

            public Step asEntity(
                Function<? super Step, ? extends Step> initialize) {
                return initialize.apply(Step.builder()
                    .description(description)
                    .pictureUrl(pictureUrl)
                    .build());
            }

            public Step asEntity() {
                return asEntity(noInit());
            }

            public Single withRecipeId(Long recipeId) {
                return this.recipeId != null && this.recipeId.equals(recipeId) ? this
                    : Single.builder()
                        .recipeId(recipeId)
                        .description(description)
                        .pictureUrl(pictureUrl)
                        .build();
            }

            private <T> Function<? super T, ? extends T> noInit() {
                return (arg) -> arg;
            }
        }
    }

    @With
    record Update(
        String description, String pictureUrl
    ) {

        @Builder
        public Update {
        }
    }

    @With
    record Result(
        Long id, Long recipeId, String description, String pictureUrl
    ) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long recipeId,

            @NotEmpty
                String description,

            String pictureUrl) {
            this.id = id;
            this.recipeId = recipeId;
            this.description = description;
            this.pictureUrl = pictureUrl;
        }

        public static Result of(Step entity) {
            return Result.builder()
                .id(entity.getId())
                .recipeId(entity.getRecipe().getId())
                .description(entity.getDescription())
                .pictureUrl(entity.getPictureUrl())
                .build();
        }

        public static List<Result> of(Streamable<Step> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }


}
