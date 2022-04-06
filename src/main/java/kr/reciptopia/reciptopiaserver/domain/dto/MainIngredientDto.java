package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface MainIngredientDto {

    interface Bulk {

        @With
        record Create(
            List<MainIngredientDto.Create> mainIngredients
        ) {

            @Builder
            public Create(
                @NotEmpty
                @Singular
                    List<MainIngredientDto.Create> mainIngredients
            ) {
                this.mainIngredients = mainIngredients;
            }

            public Set<MainIngredient> asEntity() {
                return this.mainIngredients.stream()
                    .map(MainIngredientDto.Create::asEntity)
                    .collect(Collectors.toSet());
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

        @With
        record Result(
            Map<Long, MainIngredientDto.Result> mainIngredients
        ) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, MainIngredientDto.Result> mainIngredients
            ) {
                this.mainIngredients = mainIngredients;
            }

            public static Result of(Collection<MainIngredient> mainIngredients) {
                return Result.builder()
                    .mainIngredients(mainIngredients.stream()
                        .map(MainIngredientDto.Result::of)
                        .collect(Collectors.toMap(MainIngredientDto.Result::id, result -> result)))
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
