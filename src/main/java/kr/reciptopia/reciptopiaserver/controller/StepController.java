package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.StepDto.Update;

import java.util.Set;
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
    public Result post(@Valid @RequestBody StepDto.Create.Single dto,
        Authentication authentication) {
        return service.create(dto, authentication);
    }

    @PostMapping("/post/recipe/bulk-step")
    @ResponseStatus(HttpStatus.CREATED)
    public Bulk.Result bulkCreate(@Valid @RequestBody StepDto.Bulk.Create.Single bulkDto,
        Authentication authentication) {
        return service.bulkCreate(bulkDto, authentication);
    }

    @GetMapping("/post/recipe/steps/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/post/recipe/steps")
    public Bulk.Result search(
        @RequestParam(required = false) Long recipeId,
        Pageable pageable) {
        StepSearchCondition stepSearchCondition = StepSearchCondition.builder()
            .recipeId(recipeId)
            .build();

        return service.search(stepSearchCondition, pageable);
    }

    @PatchMapping("/post/recipe/steps/{id}")
    public Result patch(@PathVariable Long id,
        @Valid @RequestBody Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @PatchMapping("/post/recipe/bulk-step")
    public Bulk.Result bulkPatch(@Valid @RequestBody Bulk.Update bulkDto,
        Authentication authentication) {
        return service.bulkUpdate(bulkDto, authentication);
    }

    @DeleteMapping("/post/recipe/steps/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }

    @DeleteMapping("/post/recipe/bulk-step/{ids}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkDelete(@PathVariable Set<Long> ids, Authentication authentication) {
        service.bulkDelete(ids, authentication);
    }
}