package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Setter
public abstract class Ingredient extends TimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ingredient_id")
	private Long id;

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "recipe_id")
	@NotNull
	private Recipe recipe;

	@NotEmpty
	private String name;

	@NotEmpty
	private String detail;

	public void setRecipe(Recipe recipe) {
		if (this.recipe != recipe) {
			if (this.recipe != null)
				this.recipe.removeIngredient(this);
			this.recipe = recipe;
			if (recipe != null)
				recipe.addIngredient(this);
		}
	}
}
