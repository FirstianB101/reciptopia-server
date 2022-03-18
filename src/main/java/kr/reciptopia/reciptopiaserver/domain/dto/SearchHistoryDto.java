package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.Set;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import lombok.Builder;
import lombok.With;

public interface SearchHistoryDto {

	@With
	record Create(
		@NotNull
		Long ownerId,

		Set<String> ingredients,

		String recipeName
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

		Set<String> ingredients,

		String recipeName
	) {

		@Builder
		public Result {
		}

		public static Result of(SearchHistory entity) {
			return Result.builder()
				.id(entity.getId())
				.ownerId(entity.getOwner().getId())
				.ingredients(entity.getIngredients())
				.recipeName(entity.getRecipeName())
				.build();
		}
	}
}
