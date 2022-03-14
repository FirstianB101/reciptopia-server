package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Step extends TimeEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "step_id")
	private Long id;

	@ToString.Exclude
	@NotNull
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
