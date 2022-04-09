package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends BaseRepository<Recipe, Long> {

}