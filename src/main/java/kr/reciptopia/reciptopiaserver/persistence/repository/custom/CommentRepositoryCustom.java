package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.CommentSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

	PageImpl<Comment> search(CommentSearchCondition commentSearchCondition, Pageable pageable);
}
