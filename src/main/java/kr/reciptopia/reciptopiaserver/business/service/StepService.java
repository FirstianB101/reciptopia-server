package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Update;

import java.util.stream.Collectors;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.StepAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.persistence.repository.StepRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.StepRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StepService {

    private final StepRepository stepRepository;
    private final StepRepositoryImpl stepRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final StepAuthorizer stepAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Recipe recipe = repoHelper.findRecipeOrThrow(dto.recipeId());
        stepAuthorizer.requireRecipeOwner(authentication, recipe);

        Step step = dto.asEntity(it -> it.withRecipe(recipe));

        return Result.of(stepRepository.save(step));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findStepOrThrow(id));
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Step step = repoHelper.findStepOrThrow(id);
        stepAuthorizer.requireStepOwner(authentication, step);

        if (dto.description() != null) {
            step.setDescription(dto.description());
        }
        if (dto.pictureUrl() != null) {
            step.setPictureUrl(dto.pictureUrl());
        }

        return Result.of(stepRepository.save(step));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Step step = repoHelper.findStepOrThrow(id);
        stepAuthorizer.requireStepOwner(authentication, step);

        stepRepository.delete(step);
    }

    public Bulk.Result search(StepSearchCondition condition, Pageable pageable) {
        return Bulk.Result.of(stepRepositoryImpl.search(condition, pageable));
    }

    @Transactional
    public Bulk.Result bulkUpdate(Bulk.Update bulkDto, Authentication authentication) {
        return Bulk.Result.builder()
            .steps(bulkDto.steps().keySet().stream()
                .map(id -> update(id, bulkDto.steps().get(id), authentication))
                .collect(Collectors.toMap(Result::id, result -> result)))
            .build();
    }
}
