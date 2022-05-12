package kr.reciptopia.reciptopiaserver.controller;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.CommentService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.CommentSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Update;
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
public class CommentController {

    private final CommentService service;

    @PostMapping("/post/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto, Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/post/comments/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/post/comments")
    public Bulk.Result search(
        @RequestParam(required = false) Long postId,
        Pageable pageable
    ) {
        CommentSearchCondition commentSearchCondition = CommentSearchCondition.builder()
            .postId(postId)
            .build();

        return service.search(commentSearchCondition, pageable);
    }

    @PatchMapping("/post/comments/{id}")
    public Result patch(@PathVariable Long id,
        @Valid @RequestBody Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @DeleteMapping("/post/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }
}
