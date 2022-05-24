package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.config.querydsl.PagingUtil.getPage;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.model.Recipe.filterByHasAllMainIngredients;
import static kr.reciptopia.reciptopiaserver.domain.model.Recipe.sortBySubIngredientCounts;

import java.util.List;
import java.util.stream.Collectors;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.RecipeAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.RecipeSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.persistence.repository.RecipeRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.RecipeRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeRepositoryImpl recipeRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final RecipeAuthorizer recipeAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Post post = repoHelper.findPostOrThrow(dto.postId());
        recipeAuthorizer.requirePostOwner(authentication, post);

        Recipe recipe = dto.asEntity().withPost(post);

        return Result.of(recipeRepository.save(recipe));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findRecipeOrThrow(id));
    }

    public Bulk.Result search(RecipeSearchCondition condition, Pageable pageable) {
        List<Recipe> recipes = recipeRepositoryImpl.search(condition)
            .stream()
            .filter(filterByHasAllMainIngredients(condition.mainIngredientNames()))
            .distinct()
            .sorted(sortBySubIngredientCounts(condition.subIngredientNames()))
            .collect(Collectors.toList());
        return Bulk.Result.of(getPage(recipes, pageable));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Recipe recipe = repoHelper.findRecipeOrThrow(id);
        recipeAuthorizer.requireRecipeOwner(authentication, recipe);

        recipeRepository.delete(recipe);
    }
}
