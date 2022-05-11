package kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition;

import lombok.Builder;

public record PostSearchCondition(
	Long ownerId, String titleLike
) {

	@Builder
	public PostSearchCondition(Long ownerId, String titleLike) {
		this.ownerId = ownerId;
		this.titleLike = titleLike;
	}

}
