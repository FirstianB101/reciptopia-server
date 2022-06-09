package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QPostImg.postImg;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostImgSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.PostImgRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class PostImgRepositoryImpl implements PostImgRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final PagingUtil pagingUtil;

	@Override
	public PageImpl<PostImg> search(
		PostImgSearchCondition searchCondition, Pageable pageable) {
		JPAQuery<PostImg> query = queryFactory
			.selectFrom(postImg)
			.where(eqPostId(searchCondition.postId()));

		return pagingUtil.getPageImpl(pageable, query, PostImg.class);
	}

	private BooleanExpression eqPostId(Long postId) {
		return postId == null ? null : postImg.post.id.eq(postId);
	}

}
