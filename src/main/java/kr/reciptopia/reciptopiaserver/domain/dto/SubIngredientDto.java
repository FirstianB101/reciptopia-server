package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.helper.InitializationHelper.noInit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
                @NotEmpty List<SubIngredientDto.Create.WithRecipe> subIngredients
            ) {

                @Builder
                public WithRecipe(
                    @Singular
                    List<SubIngredientDto.Create.WithRecipe> subIngredients
                ) {
                    this.subIngredients = subIngredients;
                }

                public Bulk.Create.Single asSingleDto(
                    Function<? super SubIngredientDto.Create.Single, ? extends SubIngredientDto.Create.Single> initialize) {
                    List<SubIngredientDto.Create.Single> singleDtos = this.subIngredients.stream()
                        .map(m -> m.asSingleDto(initialize))
                        .collect(Collectors.toList());
                    return Bulk.Create.Single.builder()
                        .subIngredients(singleDtos)
                        .build();
                }
            }

            @With
            record Single(
                @NotEmpty List<SubIngredientDto.Create.Single> subIngredients
            ) {

                @Builder
                public Single(
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
            @NotEmpty Map<Long, SubIngredientDto.Update> subIngredients
        ) {

            @Builder
            public Update(
                @Singular
                Map<Long, SubIngredientDto.Update> subIngredients
            ) {
                this.subIngredients = subIngredients;
            }
        }

        @With
        record Result(
            @NotEmpty Map<Long, SubIngredientDto.Result> subIngredients
        ) {

            @Builder
            public Result(
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

            public Create.Single asSingleDto(
                Function<? super Create.Single, ? extends Create.Single> initialize) {
                return initialize.apply(Create.Single.builder()
                    .name(name)
                    .detail(detail)
                    .build());
            }
        }

        @With
        @Builder
        record Single(
            @NotNull Long recipeId,
            @NotBlank @Size(min = 1, max = 20, message = "name은 1 ~ 20자 이여야 합니다!") String name,
            @NotBlank @Size(min = 1, max = 50, message = "detail은 1 ~ 50자 이여야 합니다!") String detail
        ) {

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

        }
    }

    @With
    @Builder
    record Update(
        @Size(min = 1, max = 20, message = "name은 1 ~ 20자 이여야 합니다!") String name,
        @Size(min = 1, max = 50, message = "detail은 1 ~ 50자 이여야 합니다!") String detail
    ) {

    }

    @With
    @Builder
    record Result(
        @NotNull Long id,
        @NotNull Long recipeId,
        @NotBlank @Size(min = 1, max = 20, message = "name은 1 ~ 20자 이여야 합니다!") String name,
        @NotBlank @Size(min = 1, max = 50, message = "detail은 1 ~ 50자 이여야 합니다!") String detail
    ) {

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
