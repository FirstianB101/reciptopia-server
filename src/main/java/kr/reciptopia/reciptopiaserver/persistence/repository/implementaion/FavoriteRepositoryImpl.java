package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QFavorite.favorite;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.FavoriteSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.FavoriteRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<Favorite> search(FavoriteSearchCondition favoriteSearchCondition,
		Pageable pageable) {
		JPAQuery<Favorite> query = queryFactory
			.selectFrom(favorite)
			.where(eqOwnerId(favoriteSearchCondition.ownerId()));

		return pagingUtil.getPageImpl(pageable, query, Favorite.class);
	}

	private BooleanExpression eqOwnerId(Long ownerId) {
		return ownerId == null ? null : favorite.owner.id.eq(ownerId);
	}

}
