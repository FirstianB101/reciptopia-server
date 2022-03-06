package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import javax.persistence.Entity;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class MainIngredient extends Ingredient {

	@Builder
	public MainIngredient(Recipe recipe, String name, String detail) {
		setRecipe(recipe);
		setName(name);
		setDetail(detail);
	}
}
