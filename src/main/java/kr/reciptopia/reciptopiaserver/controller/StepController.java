package kr.reciptopia.reciptopiaserver.controller;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.StepService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.StepDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class StepController {

    private final StepService service;

    @PostMapping("/post/recipe/steps")
    @ResponseStatus(HttpStatus.CREATED)
    public StepDto.Result post(@Valid @RequestBody StepDto.Create dto,
        Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/post/recipe/steps/{id}")
    public StepDto.Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/post/recipe/steps")
    public StepDto.Bulk.Result search(
        @RequestParam(required = false) Long recipeId,
        Pageable pageable) {
        StepSearchCondition stepSearchCondition = StepSearchCondition.builder()
            .recipeId(recipeId)
            .build();

        return service.search(stepSearchCondition, pageable);
    }

    @PatchMapping("/post/recipe/steps/{id}")
    public StepDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody StepDto.Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @DeleteMapping("/post/recipe/steps/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }
}