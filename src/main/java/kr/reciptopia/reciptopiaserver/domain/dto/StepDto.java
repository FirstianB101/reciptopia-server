package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Collection;
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
import org.springframework.data.util.Streamable;

public interface StepDto {

    interface Bulk {

        @With
        record Create(
            List<StepDto.Create> steps
        ) {

            @Builder
            public Create(
                @NotEmpty
                @Singular
                    List<StepDto.Create> steps
            ) {
                this.steps = steps;
            }

            public List<Step> asEntity() {
                return this.steps.stream()
                    .map(StepDto.Create::asEntity)
                    .collect(Collectors.toList());
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

            public static Result of(Collection<Step> entities) {
                return Result.builder()
                    .steps(entities.stream()
                        .map(StepDto.Result::of)
                        .collect(Collectors.toMap(StepDto.Result::id, result -> result)))
                    .build();
            }
        }
    }

    @With
    record Create(
        Long recipeId, String description, String pictureUrl
    ) {

        @Builder
        public Create(
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

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
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
