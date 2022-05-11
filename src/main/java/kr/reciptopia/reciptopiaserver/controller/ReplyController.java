package kr.reciptopia.reciptopiaserver.controller;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.ReplyService;
import kr.reciptopia.reciptopiaserver.business.service.spec.searchcondition.ReplySearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Update;
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
public class ReplyController {

    private final ReplyService service;

    @PostMapping("/post/comment/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto, Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/post/comment/replies/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/post/comment/replies")
    public Bulk.Result search(
        @RequestParam(required = false) Long commentId,
        Pageable pageable
    ) {
        ReplySearchCondition replySearchCondition = ReplySearchCondition.builder()
            .commentId(commentId)
            .build();

        return service.search(replySearchCondition, pageable);
    }

    @PatchMapping("/post/comment/replies/{id}")
    public Result patch(@PathVariable Long id,
        @Valid @RequestBody Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @DeleteMapping("/post/comment/replies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }
}