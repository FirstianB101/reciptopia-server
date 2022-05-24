package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.RecipeSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;

public interface RecipeRepositoryCustom {

	List<Recipe> search(RecipeSearchCondition recipeSearchCondition);
}
