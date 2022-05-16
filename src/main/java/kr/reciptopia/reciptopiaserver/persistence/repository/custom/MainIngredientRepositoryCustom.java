package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.MainIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface MainIngredientRepositoryCustom {

    PageImpl<MainIngredient> search(MainIngredientSearchCondition mainIngredientSearchCondition,
        Pageable pageable);
}
