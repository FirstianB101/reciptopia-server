package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QRecipe.recipe;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.RecipeSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.RecipeRepositoryCustom;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public List<Recipe> search(RecipeSearchCondition recipeSearchCondition) {
		return queryFactory
			.selectFrom(recipe)
			.where(
				inIds(recipeSearchCondition.ids()),
				inPostIds(recipeSearchCondition.postIds())
			).fetch();
	}

	private BooleanExpression inIds(List<Long> ids) {
		return ids.isEmpty() ? null : recipe.id.in(ids);
	}

	private BooleanExpression inPostIds(List<Long> postIds) {
		return postIds.isEmpty() ? null : recipe.post.id.in(postIds);
	}

}
