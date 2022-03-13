package kr.reciptopia.reciptopiaserver.domain.dto;

import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import javax.validation.constraints.NotNull;
import java.util.Set;

public interface SearchHistoryDto {

	static Result of(SearchHistory entity) {
		return Result.builder()
				.id(entity.getId())
				.ownerId(entity.getOwner().getId())
				.ingredients(entity.getIngredients())
				.recipeName(entity.getRecipeName())
				.build();
	}

	@Data
	@Builder
	@With
	class Create {

		@NotNull
		private Long ownerId;

		private Set<String> ingredients;

		private String recipeName;
	}

	@Data
	@Builder
	@With
	class Result {

		@NotNull
		private Long id;

		@NotNull
		private Long ownerId;

		private Set<String> ingredients;

		private String recipeName;
	}
}
