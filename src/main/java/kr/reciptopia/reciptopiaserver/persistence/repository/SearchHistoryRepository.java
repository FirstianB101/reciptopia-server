package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHistoryRepository extends BaseRepository<SearchHistory, Long> {

    Page<SearchHistory> findAllByOwnerId(Long ownerId, Pageable pageable);
}
