package kr.reciptopia.reciptopiaserver.domain.dto;

import kr.reciptopia.reciptopiaserver.domain.model.Post;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.With;
import org.springframework.data.util.Streamable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public interface PostDto {

	@Data
	@Builder
	@With
	class Create {

		@NotNull
		private Long ownerId;

		@NotNull
		private Long recipeId;

		@Singular
		private List<String> pictureUrls;

		@NotEmpty
		private String title;

		private String content;

		public Post asEntity() {
			return Post.builder()
					.pictureUrls(pictureUrls)
					.title(title)
					.content(content)
					.build();
		}
	}

	@Data
	@Builder
	@With
	class Update {

		@Singular
		private List<String> pictureUrls;

		@NotEmpty
		private String title;

		private String content;
	}

	@Data
	@Builder
	@With
	class Result {

		@NotNull
		private Long id;

		@NotNull
		private Long ownerId;

		@NotNull
		private Long recipeId;

		private List<String> pictureUrls;

		@NotEmpty
		private String title;

		private String content;

		private Long views;

		public static Result of(Post entity) {
			return Result.builder()
					.id(entity.getId())
					.ownerId(entity.getOwner().getId())
//					.recipeId(entity.getRecipe().getId())
					.pictureUrls(entity.getPictureUrls())
					.title(entity.getTitle())
					.content(entity.getContent())
					.views(entity.getViews())
					.build();
		}

		public static List<Result> of(Streamable<Post> entities) {
			return entities.stream()
					.map(post -> of(post))
					.collect(Collectors.toList());
		}
	}
}
