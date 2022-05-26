package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record AccountProfileImgSearchCondition(Long ownerId) {

	@Builder
	public AccountProfileImgSearchCondition(Long ownerId) {
		this.ownerId = ownerId;
	}

}
