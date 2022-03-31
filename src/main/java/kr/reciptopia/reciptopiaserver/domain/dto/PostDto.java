package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface PostDto {

	@With
	record Create(Long ownerId, Long recipeId,
				  String title, String content, List<String> pictureUrls) {

		@Builder
		public Create(
			@NotNull
				Long ownerId,

			@NotNull
				Long recipeId,

			@NotEmpty
				String title,

			String content,

			@Singular
				List<String> pictureUrls) {
			this.ownerId = ownerId;
			this.recipeId = recipeId;
			this.title = title;
			this.content = content;
			this.pictureUrls = pictureUrls;
		}

		public Post asEntity() {
			return Post.builder()
				.title(title)
				.content(content)
				.pictureUrls(pictureUrls)
				.build();
		}
	}

	@With
	record Update(String title, String content, List<String> pictureUrls) {

		@Builder
		public Update(
			@NotEmpty
				String title,

			String content,

			@Singular
				List<String> pictureUrls) {
			this.title = title;
			this.content = content;
			this.pictureUrls = pictureUrls;
		}
	}

	@With
	record Result(
		Long id, Long ownerId, Long recipeId,
		String title, String content, List<String> pictureUrls, Long views) {

		@Builder
		public Result(
			@NotNull
				Long id,

			@NotNull
				Long ownerId,

			@NotNull
				Long recipeId,

			@NotEmpty
				String title,

			String content,

			List<String> pictureUrls,

			Long views) {
			this.id = id;
			this.ownerId = ownerId;
			this.recipeId = recipeId;
			this.title = title;
			this.content = content;
			this.pictureUrls = pictureUrls;
			this.views = views;
		}

		public static Result of(Post entity) {
			return Result.builder()
				.id(entity.getId())
				.ownerId(entity.getOwner().getId())
//					.recipeId(entity.getRecipe().getId())
				.title(entity.getTitle())
				.content(entity.getContent())
				.pictureUrls(entity.getPictureUrls())
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
