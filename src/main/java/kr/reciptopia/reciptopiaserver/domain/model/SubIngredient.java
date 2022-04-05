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
@With
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubIngredient extends Ingredient {

	@Builder
	public SubIngredient(Recipe recipe, String name, String detail) {
		super(recipe, name, detail);
	}

	public SubIngredient withId(Long id) {
		SubIngredient subIngredient = SubIngredient.builder()
			.recipe(recipe)
			.name(name)
			.detail(detail)
			.build();
		subIngredient.setId(id);
		return this.id != null && this.id.equals(id) ? this : subIngredient;
	}

	public SubIngredient withRecipe(Recipe recipe) {
		return this.recipe != null && this.recipe.equals(recipe) ? this : SubIngredient.builder()
			.name(name)
			.detail(detail)
			.recipe(recipe)
			.build()
			.withId(id);
	}

	public SubIngredient withName(@NotEmpty String name) {
		return this.name != null && this.name.equals(name) ? this :
			SubIngredient.builder()
				.recipe(recipe)
				.name(name)
				.detail(detail)
				.build()
				.withId(id);
	}

	public SubIngredient withDetail(@NotEmpty String detail) {
		return this.detail != null && this.detail.equals(detail) ? this :
			SubIngredient.builder()
				.recipe(recipe)
				.name(name)
				.detail(detail)
				.build()
				.withId(id);
	}
}
