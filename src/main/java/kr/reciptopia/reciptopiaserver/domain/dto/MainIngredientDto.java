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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface MainIngredientDto {

    interface Bulk {

        interface Create {

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
                Map<Long, List<MainIngredientDto.Result>> mainIngredients
            ) {

                @Builder
                public PostId(
                    @NotEmpty
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
