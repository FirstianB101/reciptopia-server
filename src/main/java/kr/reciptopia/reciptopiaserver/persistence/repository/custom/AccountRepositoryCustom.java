package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Bulk;

import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {

    Bulk.Result search(Pageable pageable);
}
