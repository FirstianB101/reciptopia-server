package kr.reciptopia.reciptopiaserver.domain.dto;

import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import javax.validation.constraints.NotNull;

public interface FavoriteDto {

	static Result of(Favorite entity) {
		return Result.builder()
				.id(entity.getId())
				.ownerId(entity.getOwner().getId())
				.postId(entity.getPost().getId())
				.build();
	}

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
	}
}
