package kr.reciptopia.reciptopiaserver.persistence.repository;

import java.util.Optional;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends BaseRepository<Favorite, Long> {

}
