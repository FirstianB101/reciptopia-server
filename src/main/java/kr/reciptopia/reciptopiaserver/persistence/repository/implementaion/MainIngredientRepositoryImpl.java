package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QMainIngredient.mainIngredient;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.MainIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.MainIngredientRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class MainIngredientRepositoryImpl implements MainIngredientRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<MainIngredient> search(
		MainIngredientSearchCondition mainIngredientSearchCondition,
		Pageable pageable) {
		JPAQuery<MainIngredient> query = queryFactory
			.selectFrom(mainIngredient)
			.where(eqRecipeId(mainIngredientSearchCondition.recipeId()));

		return pagingUtil.getPageImpl(pageable, query, MainIngredient.class);
	}

	private BooleanExpression eqRecipeId(Long recipeId) {
		return recipeId == null ? null : mainIngredient.recipe.id.eq(recipeId);
	}

}
