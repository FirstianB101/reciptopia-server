package kr.reciptopia.reciptopiaserver.domain.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@ToString
@Entity
@With
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MainIngredient extends Ingredient {

    @Builder
    public MainIngredient(Recipe recipe, String name, String detail) {
        super(recipe, name, detail);
    }

    public MainIngredient withId(Long id) {
        MainIngredient mainIngredient = MainIngredient.builder()
            .recipe(recipe)
            .name(name)
            .detail(detail)
            .build();
        mainIngredient.setId(id);
        return this.id != null && this.id.equals(id) ? this : mainIngredient;
    }

    public MainIngredient withRecipe(Recipe recipe) {
        return this.recipe != null && this.recipe.equals(recipe) ? this :
            MainIngredient.builder()
                .recipe(recipe)
                .name(name)
                .detail(detail)
                .build()
                .withId(id);
    }

    public MainIngredient withName(@NotEmpty String name) {
        return this.name != null && this.name.equals(name) ? this :
            MainIngredient.builder()
                .recipe(recipe)
                .name(name)
                .detail(detail)
                .build()
                .withId(id);
    }

    public MainIngredient withDetail(@NotEmpty String detail) {
        return this.detail != null && this.detail.equals(detail) ? this :
            MainIngredient.builder()
                .recipe(recipe)
                .name(name)
                .detail(detail)
                .build()
                .withId(id);
    }
}
