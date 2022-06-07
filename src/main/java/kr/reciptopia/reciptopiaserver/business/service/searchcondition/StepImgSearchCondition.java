package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record StepImgSearchCondition(Long stepId, Long recipeId) {

	@Builder
	public StepImgSearchCondition(Long stepId, Long recipeId) {
		this.stepId = stepId;
		this.recipeId = recipeId;
	}
}
