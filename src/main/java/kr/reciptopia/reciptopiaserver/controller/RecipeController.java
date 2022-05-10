package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Result;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.Set;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.RecipeService;
import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.RecipeSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService service;

    @PostMapping("/post/recipes")
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeDto.Result post(@Valid @RequestBody RecipeDto.Create dto,
        Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/post/recipes/{id}")
    public RecipeDto.Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/post/recipes")
    public Bulk.Result search(
        @RequestParam(required = false) Set<String> mainIngredientNames,
        @RequestParam(required = false) Set<String> subIngredientNames,
        Pageable pageable
    ) {
        RecipeSearchCondition condition = RecipeSearchCondition.builder()
            .mainIngredientNames(mainIngredientNames)
            .subIngredientNames(subIngredientNames)
            .build();
        return service.search(condition, pageable);
    }

    @DeleteMapping("/post/recipes/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }
}