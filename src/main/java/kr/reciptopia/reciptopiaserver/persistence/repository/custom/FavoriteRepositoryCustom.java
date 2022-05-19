package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.FavoriteSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface FavoriteRepositoryCustom {

	PageImpl<Favorite> search(FavoriteSearchCondition favoriteSearchCondition, Pageable pageable);
}
