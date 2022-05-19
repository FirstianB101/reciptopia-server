package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record FavoriteSearchCondition(Long ownerId) {

	@Builder
	public FavoriteSearchCondition(Long ownerId) {
		this.ownerId = ownerId;
	}

}
