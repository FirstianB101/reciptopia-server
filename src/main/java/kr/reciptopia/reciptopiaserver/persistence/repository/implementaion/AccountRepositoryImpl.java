package kr.reciptopia.reciptopiaserver.persistence.repository.implementaion;

import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.model.QAccount.account;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
    public Bulk.Result search(Pageable pageable) {

        JPAQuery<Account> query = queryFactory
            .selectFrom(account);

        PageImpl<Account> pageImpl = pagingUtil.getPageImpl(pageable, query, Account.class);
        return Bulk.Result.of(pageImpl);
    }
}
