package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.model.QAccount.account;
import static kr.reciptopia.reciptopiaserver.domain.model.QPost.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountSearchCondition;
import kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.persistence.repository.custom.AccountRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final PagingUtil pagingUtil;

    @Override
    public PageImpl<?> search(AccountSearchCondition accountSearchCondition,
        Pageable pageable) {
        var accountWithPostId = queryFactory
            .from(account);
        innerJoinWithPost(accountWithPostId, accountSearchCondition.postIds())
            .where(inPostIds(accountSearchCondition.postIds()));

        return pagingUtil.getPageImpl(pageable, accountWithPostId, Account.class);
    }

    private JPAQuery<?> innerJoinWithPost(JPAQuery<?> query, List<Long> postIds) {
        return postIds.isEmpty() ?
            query :
            query
                .select(post.id, account)
                .innerJoin(post)
                .on(post.owner.eq(account));
    }

    private BooleanExpression inPostIds(List<Long> postIds) {
        return postIds.isEmpty() ? null : post.id.in(postIds);
    }
}
