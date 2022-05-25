package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import com.querydsl.core.Tuple;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.MainIngredientSearchCondition;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface MainIngredientRepositoryCustom {

    PageImpl<Tuple> search(MainIngredientSearchCondition mainIngredientSearchCondition,
        Pageable pageable);
}
