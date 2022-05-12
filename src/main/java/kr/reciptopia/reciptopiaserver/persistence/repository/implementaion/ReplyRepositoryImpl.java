package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QReply.reply;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.ReplySearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.ReplyRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ReplyRepositoryImpl implements ReplyRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<Reply> search(ReplySearchCondition replySearchCondition, Pageable pageable) {
		JPAQuery<Reply> query = queryFactory
			.selectFrom(reply)
			.where(eqCommentId(replySearchCondition.commentId()));

		return pagingUtil.getPageImpl(pageable, query, Reply.class);
	}

	private BooleanExpression eqCommentId(Long commentId) {
		return commentId == null ? null : reply.comment.id.eq(commentId);
	}

}
