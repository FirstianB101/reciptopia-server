package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record StepSearchCondition(Long recipeId) {

    @Builder
    public StepSearchCondition(Long recipeId) {
        this.recipeId = recipeId;
    }
}
