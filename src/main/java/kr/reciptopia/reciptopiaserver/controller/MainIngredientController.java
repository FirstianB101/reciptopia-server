package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.MainIngredientDto.Update;

import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.MainIngredientService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.MainIngredientSearchCondition;
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
public class MainIngredientController {

    private final MainIngredientService service;

    @PostMapping("/post/recipe/mainIngredients")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto,
        Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/post/recipe/mainIngredients/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/post/recipe/mainIngredients")
    public Bulk.Result search(
        @RequestParam(required = false) Long recipeId,
        @RequestParam(required = false) Long postId,
        @RequestParam(required = false) List<Long> ids,
        @RequestParam(required = false) List<Long> recipeIds,
        @RequestParam(required = false) List<Long> postIds,
        Pageable pageable) {
        MainIngredientSearchCondition mainIngredientSearchCondition = MainIngredientSearchCondition.builder()
            .ids(ids)
            .recipeIds(recipeIds)
            .postIds(postIds)
            .recipeId(recipeId)
            .postId(postId)
            .build();

        return service.search(mainIngredientSearchCondition, pageable);
    }


    @PatchMapping("/post/recipe/mainIngredients/{id}")
    public Result patch(@PathVariable Long id,
        @Valid @RequestBody Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @DeleteMapping("/post/recipe/mainIngredients/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }

    @PostMapping("/post/recipe/bulk-mainIngredient")
    @ResponseStatus(HttpStatus.CREATED)
    public Bulk.Result bulkCreate(@Valid @RequestBody Bulk.Create bulkDto,
        Authentication authentication) {
        return service.bulkCreate(bulkDto, authentication);
    }

    @PatchMapping("/post/recipe/bulk-mainIngredient")
    public Bulk.Result bulkPatch(@Valid @RequestBody Bulk.Update bulkDto,
        Authentication authentication) {
        return service.bulkUpdate(bulkDto, authentication);
    }

    @DeleteMapping("/post/recipe/bulk-mainIngredient/{ids}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkDelete(@PathVariable Set<Long> ids, Authentication authentication) {
        service.bulkDelete(ids, authentication);
    }
}