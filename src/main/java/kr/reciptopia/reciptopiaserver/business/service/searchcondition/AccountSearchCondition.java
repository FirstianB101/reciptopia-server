package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

public record AccountSearchCondition(List<Long> postIds) {

	@Builder
	public AccountSearchCondition(List<Long> postIds) {
		this.postIds = postIds == null ? new ArrayList<>() : postIds;
	}

}
