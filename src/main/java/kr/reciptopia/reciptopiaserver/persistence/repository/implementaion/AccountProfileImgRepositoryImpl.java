package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QAccountProfileImg.accountProfileImg;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountProfileImgSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.AccountProfileImgRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class AccountProfileImgRepositoryImpl implements AccountProfileImgRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<AccountProfileImg> search(
		AccountProfileImgSearchCondition searchCondition, Pageable pageable) {
		JPAQuery<AccountProfileImg> query = queryFactory
			.selectFrom(accountProfileImg)
			.where(eqOwnerId(searchCondition.ownerId()));

		return pagingUtil.getPageImpl(pageable, query, AccountProfileImg.class);
	}

	private BooleanExpression eqOwnerId(Long ownerId) {
		return ownerId == null ? null : accountProfileImg.owner.id.eq(ownerId);
	}

}
