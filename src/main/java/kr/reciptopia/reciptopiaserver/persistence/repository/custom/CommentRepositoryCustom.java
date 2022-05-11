package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.CommentSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Bulk;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

	Bulk.Result search(CommentSearchCondition commentSearchCondition, Pageable pageable);
}
