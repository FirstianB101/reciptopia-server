package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QComment.comment;
import static kr.reciptopia.reciptopiaserver.domain.model.QPost.post;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.CommentSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.CommentRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<Comment> search(CommentSearchCondition commentSearchCondition,
		Pageable pageable) {
		JPAQuery<Comment> query = queryFactory
			.selectFrom(comment)
			.innerJoin(comment.post, post)
			.where(eqPostId(commentSearchCondition.postId()));

		return pagingUtil.getPageImpl(pageable, query, Comment.class);
	}

	private BooleanExpression eqPostId(Long postId) {
		return postId == null ? null : comment.post.id.eq(postId);
	}

}
