package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.PostSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Bulk;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

	Bulk.Result search(PostSearchCondition postSearchCondition, Pageable pageable);
}
