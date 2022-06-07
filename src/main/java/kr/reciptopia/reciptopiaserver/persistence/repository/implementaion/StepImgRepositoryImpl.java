package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QStep.step;
import static kr.reciptopia.reciptopiaserver.domain.model.QStepImg.stepImg;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepImgSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.StepImgRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class StepImgRepositoryImpl implements StepImgRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<StepImg> search(
		StepImgSearchCondition searchCondition, Pageable pageable) {
		JPAQuery<StepImg> query = queryFactory
			.selectFrom(stepImg)
			.innerJoin(stepImg.step, step)
			.where(
				eqStepId(searchCondition.stepId()),
				eqRecipeId(searchCondition.recipeId())
			);

		return pagingUtil.getPageImpl(pageable, query, StepImg.class);
	}

	private BooleanExpression eqStepId(Long stepId) {
		return stepId == null ? null : stepImg.step.id.eq(stepId);
	}

	private BooleanExpression eqRecipeId(Long recipeId) {
		return recipeId == null ? null : step.recipe.id.eq(recipeId);
	}

}
