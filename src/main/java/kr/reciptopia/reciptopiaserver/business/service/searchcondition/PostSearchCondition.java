package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;

public record PostSearchCondition(
	Long ownerId, String titleLike, Set<String> mainIngredientNames, Set<String> subIngredientNames,
	List<Long> ids
) {

	@Builder
	public PostSearchCondition(Long ownerId, String titleLike, Set<String> mainIngredientNames,
		Set<String> subIngredientNames, List<Long> ids) {
		this.ownerId = ownerId;
		this.titleLike = titleLike;
		this.mainIngredientNames =
			mainIngredientNames == null ? new HashSet<>() : mainIngredientNames;
		this.subIngredientNames = subIngredientNames == null ? new HashSet<>() : subIngredientNames;
		this.ids = ids == null ? new ArrayList<>() : ids;
	}

}
