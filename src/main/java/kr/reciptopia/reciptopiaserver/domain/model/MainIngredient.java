package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import javax.persistence.Entity;

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
