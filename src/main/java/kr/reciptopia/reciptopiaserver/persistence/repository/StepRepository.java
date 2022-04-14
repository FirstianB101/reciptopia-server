package kr.reciptopia.reciptopiaserver.persistence.repository;

import kr.reciptopia.reciptopiaserver.domain.model.Step;
import org.springframework.stereotype.Repository;

@Repository
public interface StepRepository extends BaseRepository<Step, Long> {
    void deleteAllInBatchByRecipeId(Long recipeId);
}
