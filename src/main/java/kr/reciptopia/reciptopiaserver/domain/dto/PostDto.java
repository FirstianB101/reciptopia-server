package kr.reciptopia.reciptopiaserver.domain.dto;

import kr.reciptopia.reciptopiaserver.domain.model.Post;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.util.Streamable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

public interface PostDto {

	@With
	record Create(Long ownerId, Long recipeId,
				  List<String> pictureUrls, String title, String content) {

		@Builder
		public Create(
				@NotNull
						Long ownerId,

				@NotNull
						Long recipeId,

				@Singular
						List<String> pictureUrls,

				@NotEmpty
						String title,

				String content) {
			this.ownerId = ownerId;
			this.recipeId = recipeId;
			this.pictureUrls = pictureUrls;
			this.title = title;
			this.content = content;
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
			@NotNull
			Long id,

			@NotNull
			Long ownerId,

			@NotNull
			Long recipeId,

			List<String> pictureUrls,

			@NotEmpty
			String title,

			String content,

			Long views) {

		@Builder
		public Result {
		}

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
