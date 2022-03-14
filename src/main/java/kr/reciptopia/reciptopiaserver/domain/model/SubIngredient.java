package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import javax.persistence.Entity;

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
