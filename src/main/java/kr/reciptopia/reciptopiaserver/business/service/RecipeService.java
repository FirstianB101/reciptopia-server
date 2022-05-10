package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Result;

import java.util.List;
import java.util.stream.Collectors;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.RecipeAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.RecipeSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.persistence.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository RecipeRepository;
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

    public Bulk.Result search(RecipeSearchCondition condition, Pageable pageable) {
        List<Recipe> recipes = RecipeRepository.findAll().stream()
            .filter(condition.mainIngredientNames().isEmpty() ?
                recipe -> true :
                recipe -> recipe.hasAllMainIngredients(condition.mainIngredientNames()))
            .distinct()
            .sorted(condition.subIngredientNames().isEmpty() ?
                (o1, o2) -> (int) (o1.getId() - o2.getId()) :
                (o1, o2) -> o2.countIncludedSubIngredients(condition.subIngredientNames())
                    - o1.countIncludedSubIngredients(condition.subIngredientNames()))
            .collect(Collectors.toList());
        return Bulk.Result.of(recipes.subList(
            (int) pageable.getOffset(),
            (int) Math.min((pageable.getOffset() + pageable.getPageSize()),
                recipes.size() - pageable.getOffset())));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Recipe recipe = repoHelper.findRecipeOrThrow(id);
        recipeAuthorizer.requireRecipeOwner(authentication, recipe);

        RecipeRepository.delete(recipe);
    }
}
