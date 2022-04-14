package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import org.springframework.stereotype.Repository;

@Repository
public interface MainIngredientRepository extends BaseRepository<MainIngredient, Long> {
    void deleteAllInBatchByRecipeId(Long recipeId);
}
