package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.IngredientAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.SubIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.SubIngredient;
import kr.reciptopia.reciptopiaserver.persistence.repository.SubIngredientRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.SubIngredientRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubIngredientService {

    private final SubIngredientRepository subIngredientRepository;
    private final SubIngredientRepositoryImpl subIngredientRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final IngredientAuthorizer ingredientAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Recipe recipe = repoHelper.findRecipeOrThrow(dto.recipeId());
        ingredientAuthorizer.requireRecipeOwner(authentication, recipe);

        SubIngredient subIngredient = dto.asEntity(it -> it.withRecipe(recipe));

        return Result.of(subIngredientRepository.save(subIngredient));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findSubIngredientOrThrow(id));
    }

    public Bulk.Result search(SubIngredientSearchCondition condition, Pageable pageable) {
        return Bulk.Result.of(subIngredientRepositoryImpl.search(condition, pageable));
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        SubIngredient subIngredient = repoHelper.findSubIngredientOrThrow(id);
        Account owner = subIngredient.getRecipe().getPost().getOwner();
        ingredientAuthorizer.requireByOneself(authentication, owner);

        if (dto.name() != null) {
            subIngredient.setName(dto.name());
        }
        if (dto.detail() != null) {
            subIngredient.setDetail(dto.detail());
        }

        return Result.of(subIngredientRepository.save(subIngredient));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        SubIngredient subIngredient = repoHelper.findSubIngredientOrThrow(id);
        ingredientAuthorizer.requireIngredientOwner(authentication, subIngredient);

        subIngredientRepository.delete(subIngredient);
    }

    @Transactional
    public void update(Bulk.Update bulkSubIngredient,
        Authentication authentication) {
        bulkSubIngredient.subIngredients().keySet()
            .forEach(id -> update(id, bulkSubIngredient.subIngredients().get(id), authentication));
    }
}
