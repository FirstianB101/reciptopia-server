package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record MainIngredientSearchCondition(Long recipeId) {

    @Builder
    public MainIngredientSearchCondition(Long recipeId) {
        this.recipeId = recipeId;
    }
}
