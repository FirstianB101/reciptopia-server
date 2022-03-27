package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.Post;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends BaseRepository<Post, Long> {

	Optional<Post> findByOwnerIdAndRecipeId(Long ownerId, Long recipeId);

}
