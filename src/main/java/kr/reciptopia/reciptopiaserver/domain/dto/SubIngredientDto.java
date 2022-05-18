package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface SubIngredientDto {

    interface Bulk {

        @With
        record Create(
            List<SubIngredientDto.Create> subIngredients
        ) {

            @Builder
            public Create(
                @NotEmpty
                @Singular
                    List<SubIngredientDto.Create> subIngredients
            ) {
                this.subIngredients = subIngredients;
            }

            public Set<SubIngredient> asEntity() {
                return this.subIngredients.stream()
                    .map(SubIngredientDto.Create::asEntity)
                    .collect(Collectors.toSet());
            }
        }

        @With
        record Update(
            Map<Long, SubIngredientDto.Update> subIngredients
        ) {

            @Builder
            public Update(
                @NotEmpty
                @Singular
                    Map<Long, SubIngredientDto.Update> subIngredients
            ) {
                this.subIngredients = subIngredients;
            }
        }

        @With
        record Result(
            Map<Long, SubIngredientDto.Result> subIngredients
        ) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, SubIngredientDto.Result> subIngredients
            ) {
                this.subIngredients = subIngredients;
            }

            public static Result of(Page<SubIngredient> subIngredients) {
                return Result.builder()
                    .subIngredients(
                        (Map<? extends Long, ? extends SubIngredientDto.Result>) subIngredients.stream()
                            .map(SubIngredientDto.Result::of)
                            .collect(
                                Collectors.toMap(SubIngredientDto.Result::id,
                                    result -> result,
                                    (x, y) -> y,
                                    LinkedHashMap::new)))
                    .build();
            }
        }
    }

    @With
    record Create(
        Long recipeId, String name, String detail
    ) {

        @Builder
        public Create(
            @NotNull
                Long recipeId,

            @NotEmpty
                String name,

            @NotEmpty
                String detail) {
            this.recipeId = recipeId;
            this.name = name;
            this.detail = detail;
        }

        public SubIngredient asEntity(
            Function<? super SubIngredient, ? extends SubIngredient> initialize) {
            return initialize.apply(SubIngredient.builder()
                .name(name)
                .detail(detail)
                .build());
        }

        public SubIngredient asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Update(
        String name, String detail
    ) {

        @Builder
        public Update {
        }
    }

    @With
    record Result(
        Long id, Long recipeId, String name, String detail
    ) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long recipeId,

            @NotEmpty
                String name,

            @NotEmpty
                String detail) {
            this.id = id;
            this.recipeId = recipeId;
            this.name = name;
            this.detail = detail;
        }

        public static Result of(SubIngredient entity) {
            return Result.builder()
                .id(entity.getId())
                .recipeId(entity.getRecipe().getId())
                .name(entity.getName())
                .detail(entity.getDetail())
                .build();
        }

        public static List<Result> of(Streamable<SubIngredient> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }


}
