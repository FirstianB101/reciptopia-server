package kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;

public record RecipeSearchCondition(
    Set<String> mainIngredientNames, Set<String> subIngredientNames
) {

    @Builder
    public RecipeSearchCondition(Set<String> mainIngredientNames, Set<String> subIngredientNames) {
        this.mainIngredientNames =
            mainIngredientNames == null ? new HashSet<>() : mainIngredientNames;
        this.subIngredientNames = subIngredientNames == null ? new HashSet<>() : subIngredientNames;
    }
}