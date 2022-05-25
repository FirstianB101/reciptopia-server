package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QMainIngredient.mainIngredient;
import static kr.reciptopia.reciptopiaserver.domain.model.QPost.post;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
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
	public PageImpl<Tuple> search(
		MainIngredientSearchCondition mainIngredientSearchCondition,
		Pageable pageable) {
		JPAQuery<Tuple> ingredientWithPostId = queryFactory
			.from(post, mainIngredient)
			.select(post.id, mainIngredient)
			.where(
				post.eq(mainIngredient.recipe.post),
				eqRecipeId(mainIngredientSearchCondition.recipeId()),
				eqPostId(mainIngredientSearchCondition.postId()),
				inIds(mainIngredientSearchCondition.ids()),
				inRecipeIds(mainIngredientSearchCondition.recipeIds()),
				inPostIds(mainIngredientSearchCondition.postIds())
			);

		return pagingUtil.getPageImpl(pageable, ingredientWithPostId, MainIngredient.class);
	}

	private BooleanExpression eqRecipeId(Long recipeId) {
		return recipeId == null ? null : mainIngredient.recipe.id.eq(recipeId);
	}

	private BooleanExpression eqPostId(Long postId) {
		return postId == null ? null : mainIngredient.recipe.post.id.eq(postId);
	}

	private BooleanExpression inIds(List<Long> ids) {
		return ids.isEmpty() ? null : mainIngredient.id.in(ids);
	}

	private BooleanExpression inRecipeIds(List<Long> recipeIds) {
		return recipeIds.isEmpty() ? null : mainIngredient.recipe.id.in(recipeIds);
	}

	private BooleanExpression inPostIds(List<Long> postIds) {
		return postIds.isEmpty() ? null : mainIngredient.recipe.post.id.in(postIds);
	}
}
