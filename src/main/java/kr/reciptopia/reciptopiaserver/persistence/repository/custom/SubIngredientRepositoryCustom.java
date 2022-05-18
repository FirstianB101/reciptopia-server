package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.SubIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface SubIngredientRepositoryCustom {

	PageImpl<SubIngredient> search(SubIngredientSearchCondition subIngredientSearchCondition,
		Pageable pageable);
}
