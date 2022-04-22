package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
	@OnDelete(action = OnDeleteAction.CASCADE)
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
}
