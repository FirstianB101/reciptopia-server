package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Create.Single;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Update;
import java.util.Set;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.SubIngredientService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.SubIngredientSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.SubIngredientDto.Bulk;
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
public class SubIngredientController {

    private final SubIngredientService service;

    @PostMapping("/post/recipe/subIngredients")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Single dto,
        Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/post/recipe/subIngredients/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/post/recipe/subIngredients")
    public Bulk.Result search(
        @RequestParam(required = false) Long recipeId,
        Pageable pageable) {
        SubIngredientSearchCondition subIngredientSearchCondition = SubIngredientSearchCondition
            .builder()
            .recipeId(recipeId)
            .build();

        return service.search(subIngredientSearchCondition, pageable);
    }

    @PatchMapping("/post/recipe/subIngredients/{id}")
    public Result patch(@PathVariable Long id,
        @Valid @RequestBody Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @DeleteMapping("/post/recipe/subIngredients/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }

    @PostMapping("/post/recipe/bulk-subIngredient")
    @ResponseStatus(HttpStatus.CREATED)
    public Bulk.Result bulkCreate(@Valid @RequestBody Bulk.Create.Single bulkDto,
        Authentication authentication) {
        return service.bulkCreate(bulkDto, authentication);
    }

    @PatchMapping("/post/recipe/bulk-subIngredient")
    public Bulk.Result bulkPatch(@Valid @RequestBody Bulk.Update bulkDto,
        Authentication authentication) {
        return service.bulkUpdate(bulkDto, authentication);
    }

    @DeleteMapping("/post/recipe/bulk-subIngredient/{ids}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkDelete(@PathVariable Set<Long> ids, Authentication authentication) {
        service.bulkDelete(ids, authentication);
    }

}