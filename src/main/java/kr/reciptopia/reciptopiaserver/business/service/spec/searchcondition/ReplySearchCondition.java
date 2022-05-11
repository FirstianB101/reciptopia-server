package kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition;

import lombok.Builder;

public record ReplySearchCondition(Long commentId) {

	@Builder
	public ReplySearchCondition(Long commentId) {
		this.commentId = commentId;
	}

}
