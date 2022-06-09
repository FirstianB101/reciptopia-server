package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface StepImgRepositoryCustom {

	PageImpl<StepImg> search(
		StepImgSearchCondition searchCondition, Pageable pageable);

}
