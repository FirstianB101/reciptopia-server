package kr.reciptopia.reciptopiaserver.persistence.repository;

import java.util.Optional;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends BaseRepository<Account, Long> {

    Optional<Account> findByEmail(String username);
}