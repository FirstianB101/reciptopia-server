package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.PostSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.PostRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<Post> search(PostSearchCondition postSearchCondition, Pageable pageable) {
		JPAQuery<Post> query = queryFactory
			.selectFrom(post)
			.where(
				eqOwnerId(postSearchCondition.ownerId()),
				likeTitle(postSearchCondition.titleLike())
			);

		return pagingUtil.getPageImpl(pageable, query, Post.class);
	}

	private BooleanExpression eqOwnerId(Long ownerId) {
		return ownerId == null ? null : post.owner.id.eq(ownerId);
	}

	private BooleanExpression likeTitle(String titleLike) {
		if (titleLike == null || titleLike.isEmpty()) {
			return null;
		}
		return post.title.like("%" + titleLike + "%");
	}

}
