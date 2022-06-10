package kr.reciptopia.reciptopiaserver.domain.dto;

import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface MainIngredientDto {

    interface Bulk {

        interface Create {

            record WithRecipe(
                List<MainIngredientDto.Create.WithRecipe> mainIngredients
            ) {

                @Builder
                public WithRecipe(
                    @NotEmpty
                    @Singular
                        List<MainIngredientDto.Create.WithRecipe> mainIngredients
                ) {
                    this.mainIngredients = mainIngredients;
                }

                public Single asSingleDto(
                    Function<? super MainIngredientDto.Create.Single, ? extends MainIngredientDto.Create.Single> initialize) {
                    List<MainIngredientDto.Create.Single> singleDtos = this.mainIngredients.stream()
                        .map(m -> m.asSingleDto(initialize))
                        .collect(Collectors.toList());
                    return Single.builder()
                        .mainIngredients(singleDtos)
                        .build();
                }
            }

            @With
            record Single(
                List<MainIngredientDto.Create.Single> mainIngredients
            ) {

                @Builder
                public Single(
                    @NotEmpty
                    @Singular
                        List<MainIngredientDto.Create.Single> mainIngredients
                ) {
                    this.mainIngredients = mainIngredients;
                }

                public Set<MainIngredient> asEntity() {
                    return this.mainIngredients.stream()
                        .map(MainIngredientDto.Create.Single::asEntity)
                        .collect(Collectors.toSet());
                }
            }
        }

        @With
        record Update(
            Map<Long, MainIngredientDto.Update> mainIngredients
        ) {

            @Builder
            public Update(
                @NotEmpty
                @Singular
                    Map<Long, MainIngredientDto.Update> mainIngredients
            ) {
                this.mainIngredients = mainIngredients;
            }
        }

        interface ResultGroupBy {

            @With
            record PostId(
                @NotEmpty Map<Long, List<MainIngredientDto.Result>> mainIngredients
            ) {

                @Builder
                public PostId(
                    @Singular
                        Map<Long, List<MainIngredientDto.Result>> mainIngredients
                ) {
                    this.mainIngredients = mainIngredients;
                }

                public static PostId of(Page<Tuple> mainIngredients) {
                    return PostId.builder()
                        .mainIngredients(
                            (Map<? extends Long, ? extends List<MainIngredientDto.Result>>) mainIngredients.stream()
                                .collect(
                                    Collectors.toMap(tuple -> tuple.get(0, Long.class),
                                        result -> {
                                            var results = new ArrayList<MainIngredientDto.Result>();
                                            results.add(MainIngredientDto.Result.of(
                                                Objects.requireNonNull(
                                                    result.get(1, MainIngredient.class))));
                                            return results;
                                        },
                                        (x, y) -> {
                                            x.addAll(y);
                                            return x;
                                        },
                                        LinkedHashMap::new)))
                        .build();
                }
            }

            @With
            record Id(
                Map<Long, MainIngredientDto.Result> mainIngredients
            ) {

                @Builder
                public Id(
                    @NotEmpty
                    @Singular
                        Map<Long, MainIngredientDto.Result> mainIngredients
                ) {
                    this.mainIngredients = mainIngredients;
                }

                public static Id of(Page<MainIngredient> mainIngredients) {
                    return Id.builder()
                        .mainIngredients(
                            (Map<? extends Long, ? extends MainIngredientDto.Result>) mainIngredients.stream()
                                .map(MainIngredientDto.Result::of)
                                .collect(
                                    Collectors.toMap(MainIngredientDto.Result::id,
                                        result -> result,
                                        (x, y) -> y,
                                        LinkedHashMap::new)))
                        .build();
                }
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

            public Single asSingleDto(
                Function<? super Single, ? extends Single> initialize) {
                return initialize.apply(Single.builder()
                    .name(name)
                    .detail(detail)
                    .build());
            }
        }

        @With
        record Single(
            @NotNull Long recipeId,
            @NotBlank @Size(min = 1, max = 20, message = "name은 1 ~ 20자 이여야 합니다!") String name,
            @NotBlank @Size(min = 1, max = 50, message = "detail은 1 ~ 50자 이여야 합니다!") String detail
        ) {

            @Builder
            public Single {

            }

            public MainIngredient asEntity(
                Function<? super MainIngredient, ? extends MainIngredient> initialize) {
                return initialize.apply(MainIngredient.builder()
                    .name(name)
                    .detail(detail)
                    .build());
            }

            public MainIngredient asEntity() {
                return asEntity(noInit());
            }

            private <T> Function<? super T, ? extends T> noInit() {
                return (arg) -> arg;
            }

        }
    }

    @With
    record Update(
        @Size(min = 1, max = 20, message = "name은 1 ~ 20자 이여야 합니다!") String name,
        @Size(min = 1, max = 50, message = "detail은 1 ~ 50자 이여야 합니다!") String detail
    ) {

        @Builder
        public Update {
        }
    }

    @With
    record Result(
        @NotNull Long id,
        @NotNull Long recipeId,
        @NotBlank @Size(min = 1, max = 20, message = "name은 1 ~ 20자 이여야 합니다!") String name,
        @NotBlank @Size(min = 1, max = 50, message = "detail은 1 ~ 50자 이여야 합니다!") String detail
    ) {

        @Builder
        public Result {

        }

        public static Result of(MainIngredient mainIngredient) {
            return Result.builder()
                .id(mainIngredient.getId())
                .recipeId(mainIngredient.getRecipe().getId())
                .name(mainIngredient.getName())
                .detail(mainIngredient.getDetail())
                .build();
        }

        public static List<Result> of(Streamable<MainIngredient> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }


}
