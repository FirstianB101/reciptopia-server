package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface StepRepositoryCustom {

    PageImpl<Step> search(StepSearchCondition stepSearchCondition, Pageable pageable);
}
