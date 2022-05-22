package kr.reciptopia.reciptopiaserver.controller;

import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.PostService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class PostController {

    private final PostService service;

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto, Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/posts/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/posts")
    public Bulk.ResultWithCommentAndLikeTagCount search(
        @RequestParam(required = false) Long ownerId,
        @RequestParam(required = false) String titleLike,
        @RequestParam(required = false) Set<String> mainIngredientNames,
        @RequestParam(required = false) Set<String> subIngredientNames,
        @RequestParam(required = false) List<Long> ids,
        Pageable pageable
    ) {
        PostSearchCondition postSearchCondition = PostSearchCondition.builder()
            .ownerId(ownerId)
            .titleLike(titleLike)
            .mainIngredientNames(mainIngredientNames)
            .subIngredientNames(subIngredientNames)
            .ids(ids)
            .build();
        return service.search(postSearchCondition, pageable);
    }

    @PatchMapping("/posts/{id}")
    public Result patch(@PathVariable Long id,
        @Valid @RequestBody Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }
}
