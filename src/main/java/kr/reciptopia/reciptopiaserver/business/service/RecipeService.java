package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Result;

import kr.reciptopia.reciptopiaserver.business.service.authorizer.RecipeAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.persistence.repository.MainIngredientRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.RecipeRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.StepRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.SubIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository RecipeRepository;
    private final StepRepository stepRepository;
    private final MainIngredientRepository mainIngredientRepository;
    private final SubIngredientRepository subIngredientRepository;
    private final RepositoryHelper repoHelper;
    private final RecipeAuthorizer recipeAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Post post = repoHelper.findPostOrThrow(dto.postId());
        recipeAuthorizer.requirePostOwner(authentication, post);

        Recipe recipe = dto.asEntity().withPost(post);

        return Result.of(RecipeRepository.save(recipe));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findRecipeOrThrow(id));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Recipe recipe = repoHelper.findRecipeOrThrow(id);
        recipeAuthorizer.requireRecipeOwner(authentication, recipe);

        stepRepository.deleteAllInBatchByRecipeId(recipe.getId());
        mainIngredientRepository.deleteAllInBatchByRecipeId(recipe.getId());
        subIngredientRepository.deleteAllInBatchByRecipeId(recipe.getId());
        RecipeRepository.delete(recipe);
    }
}
