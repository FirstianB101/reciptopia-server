package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.Post;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends BaseRepository<Post, Long> {

	void deleteAllInBatchByOwnerId(Long ownerId);
}
