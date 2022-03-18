package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import lombok.Builder;
import lombok.With;

public interface FavoriteDto {

	@With
	record Create(
		@NotNull
		Long ownerId,

		@NotNull
		Long postId
	) {

		@Builder
		public Create {
		}
	}

	@With
	record Result(
		@NotNull
		Long id,

		@NotNull
		Long ownerId,

		@NotNull
		Long postId
	) {

		@Builder
		public Result {
		}

		public static Result of(Favorite entity) {
			return Result.builder()
				.id(entity.getId())
				.ownerId(entity.getOwner().getId())
				.postId(entity.getPost().getId())
				.build();
		}
	}
}
