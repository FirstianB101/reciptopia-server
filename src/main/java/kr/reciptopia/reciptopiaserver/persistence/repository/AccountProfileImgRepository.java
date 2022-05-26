package kr.reciptopia.reciptopiaserver.persistence.repository;

import java.util.Optional;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountProfileImgRepository extends BaseRepository<AccountProfileImg, Long> {

	Optional<AccountProfileImg> findByOwnerId(Long ownerId);
}
