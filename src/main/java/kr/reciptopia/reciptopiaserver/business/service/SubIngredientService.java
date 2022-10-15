package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;

import java.util.Set;
import java.util.stream.Collectors;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.IngredientAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.SubIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto;
import kr.reciptopia.reciptopiaserver.domain.dto.RecipePostDto;
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
    private final ServiceErrorHelper errorHelper;

    @Transactional
    public Result create(Single dto, Authentication authentication) {
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
        ingredientAuthorizer.requireIngredientOwner(authentication, subIngredient);

        if (dto.name() != null) {
            throwExceptionWhenBlankName(dto.name());
            subIngredient.setName(dto.name());
        }
        if (dto.detail() != null) {
            throwExceptionWhenBlankDetail(dto.detail());
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
    public Bulk.Result bulkCreate(Bulk.Create.Single bulkDto, Authentication authentication) {
        return Bulk.Result.builder()
            .subIngredients(bulkDto.subIngredients().stream()
                .map(dto -> create(dto, authentication))
                .collect(Collectors.toMap(Result::id, result -> result)))
            .build();
    }

    @Transactional
    public Bulk.Result bulkCreate(RecipePostDto.Create dto, Authentication authentication,
        RecipeDto.Result recipeResult) {
        Bulk.Create.WithRecipe subIngredientBulkDto = dto.subIngredients();
        Bulk.Create.Single subIngredientBulkSingle = subIngredientBulkDto.asSingleDto(
            it -> it.withRecipeId(recipeResult.id()));
        return bulkCreate(
            subIngredientBulkSingle, authentication);
    }

    @Transactional
    public Bulk.Result bulkUpdate(Bulk.Update bulkDto, Authentication authentication) {
        return Bulk.Result.builder()
            .subIngredients(bulkDto.subIngredients().keySet().stream()
                .map(id -> update(id, bulkDto.subIngredients().get(id), authentication))
                .collect(Collectors.toMap(Result::id, result -> result)))
            .build();
    }

    @Transactional
    public void bulkDelete(Set<Long> ids, Authentication authentication) {
        ids.forEach(id -> delete(id, authentication));
    }


    public void throwExceptionWhenBlankName(String name) {
        if (name.isBlank()) {
            throw errorHelper.badRequest(
                "Name must not be null and must contain at least one non-whitespace character");
        }
    }

    public void throwExceptionWhenBlankDetail(String detail) {
        if (detail.isBlank()) {
            throw errorHelper.badRequest(
                "Detail must not be null and must contain at least one non-whitespace character");
        }
    }
}
