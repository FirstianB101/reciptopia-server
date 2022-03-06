package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.FetchType.LAZY;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Step {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "step_id")
	private Long id;

	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "recipe_id")
	private Recipe recipe;

	@NotEmpty
	private String description;

	private String pictureUrl;

	@Builder
	public Step(Recipe recipe, String description, String pictureUrl) {
		setRecipe(recipe);
		setDescription(description);
		setPictureUrl(pictureUrl);
	}

	public void setRecipe(Recipe recipe) {
		if (this.recipe != recipe) {
			if (this.recipe != null)
				this.recipe.removeStep(this);
			this.recipe = recipe;
			if (recipe != null)
				recipe.addStep(this);
		}
	}
}
