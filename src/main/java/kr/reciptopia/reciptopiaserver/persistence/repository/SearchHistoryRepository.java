package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryRepository extends BaseRepository<SearchHistory, Long> {
    void deleteAllInBatchByOwnerId(Long ownerId);
}
