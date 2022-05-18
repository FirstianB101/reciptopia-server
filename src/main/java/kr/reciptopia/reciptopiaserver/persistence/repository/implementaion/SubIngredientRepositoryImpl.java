package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QSubIngredient.subIngredient;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.SubIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.SubIngredientRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class SubIngredientRepositoryImpl implements SubIngredientRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<SubIngredient> search(
		SubIngredientSearchCondition subIngredientSearchCondition,
		Pageable pageable) {
		JPAQuery<SubIngredient> query = queryFactory
			.selectFrom(subIngredient)
			.where(eqRecipeId(subIngredientSearchCondition.recipeId()));

		return pagingUtil.getPageImpl(pageable, query, SubIngredient.class);
	}

	private BooleanExpression eqRecipeId(Long recipeId) {
		return recipeId == null ? null : subIngredient.recipe.id.eq(recipeId);
	}

}
