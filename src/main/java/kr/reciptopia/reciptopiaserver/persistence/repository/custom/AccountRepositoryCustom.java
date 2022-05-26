package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountSearchCondition;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {

    PageImpl<?> search(AccountSearchCondition condition, Pageable pageable);
}
