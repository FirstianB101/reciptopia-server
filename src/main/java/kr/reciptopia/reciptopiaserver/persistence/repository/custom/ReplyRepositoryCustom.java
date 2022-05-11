package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.ReplySearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface ReplyRepositoryCustom {

	PageImpl<Reply> search(ReplySearchCondition replySearchCondition, Pageable pageable);
}
