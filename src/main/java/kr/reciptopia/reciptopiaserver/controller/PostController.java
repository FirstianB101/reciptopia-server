package kr.reciptopia.reciptopiaserver.controller;

import java.util.List;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.PostService;
import kr.reciptopia.reciptopiaserver.business.service.spec.PostSpecs;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public List<Result> search(
        @RequestParam(required = false) Long ownerId,
        @RequestParam(required = false) Long recipeId,
        @RequestParam(required = false) Long commentId,
        @RequestParam(required = false) String title,
        Pageable pageable
    ) {
        Specification<Post> spec = null;
        if (ownerId != null) {
            spec = PostSpecs.isOwner(ownerId).and(spec);
        }
        if (recipeId != null) {
            spec = PostSpecs.hasRecipe(recipeId).and(spec);
        }
        if (commentId != null) {
            spec = PostSpecs.hasComment(commentId).and(spec);
        }
        if (title != null) {
            spec = PostSpecs.titleLike(title).and(spec);
        }

        return service.search(spec, pageable);
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
