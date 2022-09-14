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


	public RecipeSearchCondition getRecipeSearchCondition() {
        return RecipeSearchCondition.builder()
            .mainIngredientNames(this.mainIngredientNames())
            .subIngredientNames(this.subIngredientNames()).build();
    }

    private PostSearchCondition withIds(List<Long> ids) {
        return PostSearchCondition.builder()
            .ownerId(this.ownerId())
            .titleLike(this.titleLike())
            .mainIngredientNames(this.mainIngredientNames())
            .subIngredientNames(this.subIngredientNames())
            .ids(ids)
            .build();
    }

    public PostSearchCondition updateConditionWithRecipeCondition(
        RecipeSearchCondition recipeSearchCondition, List<Long> postIds) {
        if (!recipeSearchCondition.isEmpty()) {
            List<Long> conditionIds = this.ids();
            if (!this.ids().isEmpty())
                conditionIds.retainAll(postIds);
            else
                conditionIds = postIds;
            return this.withIds(conditionIds);
        }
        return this;
	}
}
