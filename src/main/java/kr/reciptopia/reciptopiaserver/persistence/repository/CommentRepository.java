package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends BaseRepository<Comment, Long> {
}
