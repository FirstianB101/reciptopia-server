package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;

public record RecipeSearchCondition(
    Set<String> mainIngredientNames, Set<String> subIngredientNames, List<Long> ids,
    List<Long> postIds
) {

    @Builder
    public RecipeSearchCondition(Set<String> mainIngredientNames, Set<String> subIngredientNames,
        List<Long> ids, List<Long> postIds) {
        this.mainIngredientNames =
            mainIngredientNames == null ? new HashSet<>() : mainIngredientNames;
        this.subIngredientNames = subIngredientNames == null ? new HashSet<>() : subIngredientNames;
        this.ids = ids == null ? new ArrayList<>() : ids;
        this.postIds = postIds == null ? new ArrayList<>() : postIds;
    }

    public boolean isEmpty() {
        return this.mainIngredientNames.isEmpty() && this.subIngredientNames.isEmpty();
    }

    public boolean isEmptyCondition() {
        return this.mainIngredientNames.isEmpty() &&
            this.subIngredientNames.isEmpty() &&
            this.ids.isEmpty() &&
            this.postIds.isEmpty();

    }
}
