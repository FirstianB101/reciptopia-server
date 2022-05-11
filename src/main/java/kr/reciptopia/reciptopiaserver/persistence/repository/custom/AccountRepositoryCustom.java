package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.domain.model.Account;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {

    PageImpl<Account> search(Pageable pageable);
}
