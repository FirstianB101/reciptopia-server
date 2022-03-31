package kr.reciptopia.reciptopiaserver.persistence.repository;

import java.util.Optional;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends BaseRepository<Post, Long> {

	Optional<Post> findByOwnerIdAndRecipeId(Long ownerId, Long recipeId);

}
