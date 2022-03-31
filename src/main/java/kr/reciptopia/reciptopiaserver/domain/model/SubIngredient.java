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
public class SubIngredient extends Ingredient {

	protected SubIngredient() {
		super();
	}

	@Builder
	public SubIngredient(Recipe recipe, String name, String detail) {
		setRecipe(recipe);
		setName(name);
		setDetail(detail);
	}
}
