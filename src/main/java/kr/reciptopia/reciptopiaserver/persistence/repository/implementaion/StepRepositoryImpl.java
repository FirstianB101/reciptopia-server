package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QStep.step;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.StepRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class StepRepositoryImpl implements StepRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<Step> search(StepSearchCondition stepSearchCondition,
		Pageable pageable) {
		JPAQuery<Step> query = queryFactory
			.selectFrom(step)
			.where(eqRecipeId(stepSearchCondition.recipeId()));

		return pagingUtil.getPageImpl(pageable, query, Step.class);
	}

	private BooleanExpression eqRecipeId(Long recipeId) {
		return recipeId == null ? null : step.recipe.id.eq(recipeId);
	}

}
