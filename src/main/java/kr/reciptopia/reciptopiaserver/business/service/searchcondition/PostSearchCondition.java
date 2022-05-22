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


	public static RecipeSearchCondition getRecipeSearchCondition(
		PostSearchCondition postSearchCondition) {
		return RecipeSearchCondition.builder()
			.mainIngredientNames(postSearchCondition.mainIngredientNames())
			.subIngredientNames(postSearchCondition.subIngredientNames()).build();
	}

	private static PostSearchCondition withIds(PostSearchCondition condition, List<Long> ids) {
		return PostSearchCondition.builder()
			.ownerId(condition.ownerId())
			.titleLike(condition.titleLike())
			.mainIngredientNames(condition.mainIngredientNames())
			.subIngredientNames(condition.subIngredientNames())
			.ids(ids)
			.build();
	}

	public static PostSearchCondition updateConditionWithRecipeCondition(
		PostSearchCondition condition,
		RecipeSearchCondition recipeSearchCondition, List<Long> postIds) {
		if (!recipeSearchCondition.isEmpty()) {
			List<Long> conditionIds = condition.ids();
			if (!conditionIds.isEmpty())
				conditionIds.retainAll(postIds);
			else
				conditionIds = postIds;
			condition = withIds(condition, conditionIds);
		}
		return condition;
	}
}
