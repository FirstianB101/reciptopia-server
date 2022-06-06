package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record PostImgSearchCondition(Long postId) {

	@Builder
	public PostImgSearchCondition(Long postId) {
		this.postId = postId;
	}

}
