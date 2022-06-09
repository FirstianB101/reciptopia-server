package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QPostLikeTag.postLikeTag;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostLikeTagSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.PostLikeTagRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class PostLikeTagRepositoryImpl implements PostLikeTagRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final PagingUtil pagingUtil;

    @Override
    public PageImpl<PostLikeTag> search(PostLikeTagSearchCondition postLikeTagSearchCondition,
        Pageable pageable) {
        JPAQuery<PostLikeTag> query = queryFactory
            .selectFrom(postLikeTag)
            .where(
                inIds(postLikeTagSearchCondition.ids()),
                inOwnerIds(postLikeTagSearchCondition.ownerIds())
            );

        return pagingUtil.getPageImpl(pageable, query, PostLikeTag.class);
    }

    private BooleanExpression inIds(List<Long> ids) {
        return ids.isEmpty() ? null : postLikeTag.id.in(ids);
    }

    private BooleanExpression inOwnerIds(List<Long> ownerIds) {
        return ownerIds.isEmpty() ? null : postLikeTag.owner.id.in(ownerIds);
    }
}