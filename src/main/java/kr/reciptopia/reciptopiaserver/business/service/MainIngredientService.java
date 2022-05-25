package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Update;

import java.util.Set;
import java.util.stream.Collectors;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.IngredientAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.MainIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.MainIngredient;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.persistence.repository.MainIngredientRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.MainIngredientRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainIngredientService {

    private final MainIngredientRepository mainIngredientRepository;
    private final MainIngredientRepositoryImpl mainIngredientRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final IngredientAuthorizer ingredientAuthorizer;

    @Transactional
    public Result create(Single dto, Authentication authentication) {
        Recipe recipe = repoHelper.findRecipeOrThrow(dto.recipeId());
        ingredientAuthorizer.requireRecipeOwner(authentication, recipe);

        MainIngredient mainIngredient = dto.asEntity(it -> it.withRecipe(recipe));

        return Result.of(mainIngredientRepository.save(mainIngredient));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findMainIngredientOrThrow(id));
    }

    public Bulk.ResultGroupBy.PostId search(MainIngredientSearchCondition condition,
        Pageable pageable) {
        return Bulk.ResultGroupBy.PostId.of(
            mainIngredientRepositoryImpl.search(condition, pageable));
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
    public Bulk.ResultGroupBy.Id bulkUpdate(Bulk.Update bulkDto, Authentication authentication) {
        return Bulk.ResultGroupBy.Id.builder()
            .mainIngredients(bulkDto.mainIngredients().keySet().stream()
                .map(id -> update(id, bulkDto.mainIngredients().get(id), authentication))
                .collect(Collectors.toMap(Result::id, result -> result)))
            .build();
    }

    @Transactional
    public Bulk.ResultGroupBy.Id bulkCreate(Bulk.Create.Single bulkDto,
        Authentication authentication) {
        return Bulk.ResultGroupBy.Id.builder()
            .mainIngredients(bulkDto.mainIngredients().stream()
                .map(dto -> create(dto, authentication))
                .collect(Collectors.toMap(Result::id, result -> result)))
            .build();
    }

    @Transactional
    public void bulkDelete(Set<Long> ids, Authentication authentication) {
        ids.forEach(id -> delete(id, authentication));
    }
}
