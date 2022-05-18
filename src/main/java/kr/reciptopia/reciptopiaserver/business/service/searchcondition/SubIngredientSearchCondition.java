package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record SubIngredientSearchCondition(Long recipeId) {

    @Builder
    public SubIngredientSearchCondition(Long recipeId) {
        this.recipeId = recipeId;
    }
}
