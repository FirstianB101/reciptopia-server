package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.RecipePostDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipePostDto.Result;

import kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto;
import kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto;
import kr.reciptopia.reciptopiaserver.domain.dto.StepDto;
import kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipePostService {

    private final PostService postService;
    private final RecipeService recipeService;
    private final MainIngredientService mainIngredientService;
    private final SubIngredientService subIngredientService;
    private final StepService stepService;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        PostDto.Result postResult = postService.create(dto.post(), authentication);

        RecipeDto.Result recipeResult = recipeService.create(authentication, postResult);

        MainIngredientDto.Bulk.ResultGroupBy.Id mainIngredientBulkResult =
            mainIngredientService.bulkCreate(dto, authentication, recipeResult);

        SubIngredientDto.Bulk.Result subIngredientBulkResult =
            subIngredientService.bulkCreate(dto, authentication, recipeResult);

        StepDto.Bulk.Result stepBulkResult =
            stepService.bulkCreate(dto, authentication, recipeResult);

        return Result.builder()
            .post(postResult)
            .recipe(recipeResult)
            .bulkMainIngredient(mainIngredientBulkResult)
            .bulkSubIngredient(subIngredientBulkResult)
            .bulkStep(stepBulkResult)
            .build();
    }

}
