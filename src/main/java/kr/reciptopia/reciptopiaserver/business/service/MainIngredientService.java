package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Update;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.IngredientAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.persistence.repository.MainIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainIngredientService {

    private final MainIngredientRepository mainIngredientRepository;
    private final RepositoryHelper repoHelper;
    private final IngredientAuthorizer ingredientAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Recipe recipe = repoHelper.findRecipeOrThrow(dto.recipeId());
        ingredientAuthorizer.requireRecipeOwner(authentication, recipe);

        MainIngredient mainIngredient = dto.asEntity(it -> it.withRecipe(recipe));

        return Result.of(mainIngredientRepository.save(mainIngredient));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findMainIngredientOrThrow(id));
    }

    public List<Result> search(Specification<MainIngredient> spec, Pageable pageable) {
        Page<MainIngredient> entities = mainIngredientRepository.findAll(spec, pageable);
        return Result.of(entities);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        MainIngredient mainIngredient = repoHelper.findMainIngredientOrThrow(id);
        ingredientAuthorizer.requireIngredientOwner(authentication, mainIngredient);

        if (dto.name() != null) {
            mainIngredient.setName(dto.name());
        }
        if (dto.detail() != null) {
            mainIngredient.setDetail(dto.detail());
        }

        return Result.of(mainIngredientRepository.save(mainIngredient));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        MainIngredient mainIngredient = repoHelper.findMainIngredientOrThrow(id);
        ingredientAuthorizer.requireIngredientOwner(authentication, mainIngredient);

        mainIngredientRepository.delete(mainIngredient);
    }

    @Transactional
    public void update(Bulk.Update bulkMainIngredient, Authentication authentication) {
        bulkMainIngredient.mainIngredients().keySet()
            .forEach(
                id -> update(id, bulkMainIngredient.mainIngredients().get(id), authentication));
    }
}
