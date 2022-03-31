package kr.reciptopia.reciptopiaserver.domain.model;

import javax.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@ToString
@With
@Entity
public class MainIngredient extends Ingredient {

    protected MainIngredient() {
        super();
    }

    @Builder
    public MainIngredient(Recipe recipe, String name, String detail) {
        setRecipe(recipe);
        setName(name);
        setDetail(detail);
    }
}
