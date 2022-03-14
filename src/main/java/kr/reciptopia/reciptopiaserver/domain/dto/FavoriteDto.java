package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface FavoriteDto {

	@Data
	@Builder
	@With
	class Create {

		@NotNull
		private Long ownerId;

		@NotNull
		private Long postId;
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
		private Long postId;

		public static Result of(Favorite entity) {
			return Result.builder()
				.id(entity.getId())
				.ownerId(entity.getOwner().getId())
				.postId(entity.getPost().getId())
				.build();
		}
	}
}
