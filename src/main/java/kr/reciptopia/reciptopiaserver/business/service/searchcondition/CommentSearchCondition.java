package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import lombok.Builder;

public record CommentSearchCondition(Long postId) {

	@Builder
	public CommentSearchCondition(Long postId) {
		this.postId = postId;
	}

}
