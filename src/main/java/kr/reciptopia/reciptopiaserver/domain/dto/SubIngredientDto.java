package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.helper.CollectorHelper.noInit;
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

        interface Create {

            record WithRecipe(
                List<SubIngredientDto.Create.WithRecipe> subIngredients
            ) {

                @Builder
                public WithRecipe(
                    @NotEmpty
                    @Singular
                        List<SubIngredientDto.Create.WithRecipe> subIngredients
                ) {
                    this.subIngredients = subIngredients;
                }

                public SubIngredientDto.Bulk.Create.Single asSingleDto(
                    Function<? super SubIngredientDto.Create.Single, ? extends SubIngredientDto.Create.Single> initialize) {
                    List<SubIngredientDto.Create.Single> singleDtos = this.subIngredients.stream()
                        .map(m -> m.asSingleDto(initialize))
                        .collect(Collectors.toList());
                    return SubIngredientDto.Bulk.Create.Single.builder()
                        .subIngredients(singleDtos)
                        .build();
                }
            }

            @With
            record Single(
                List<SubIngredientDto.Create.Single> subIngredients
            ) {

                @Builder
                public Single(
                    @NotEmpty
                    @Singular
                        List<SubIngredientDto.Create.Single> subIngredients
                ) {
                    this.subIngredients = subIngredients;
                }

                public Set<SubIngredient> asEntity() {
                    return this.subIngredients.stream()
                        .map(SubIngredientDto.Create.Single::asEntity)
                        .collect(Collectors.toSet());
                }
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

    interface Create {

        record WithRecipe(
            String name, String detail
        ) {

            @Builder
            public WithRecipe(

                @NotEmpty
                    String name,

                @NotEmpty
                    String detail) {
                this.name = name;
                this.detail = detail;
            }

            public SubIngredientDto.Create.Single asSingleDto(
                Function<? super SubIngredientDto.Create.Single, ? extends SubIngredientDto.Create.Single> initialize) {
                return initialize.apply(SubIngredientDto.Create.Single.builder()
                    .name(name)
                    .detail(detail)
                    .build());
            }
        }

        @With
        record Single(
            Long recipeId, String name, String detail
        ) {

            @Builder
            public Single(
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

            public Single withRecipeId(Long recipeId) {
                return this.recipeId != null && this.recipeId.equals(recipeId) ? this
                    : Single.builder()
                        .recipeId(recipeId)
                        .name(name)
                        .detail(detail)
                        .build();
            }

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
