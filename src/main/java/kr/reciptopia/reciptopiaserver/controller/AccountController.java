package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.CheckDuplicationResult;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Update;

import java.util.List;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.AccountService;
import kr.reciptopia.reciptopiaserver.business.service.spec.AccountSpecs;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
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
public class AccountController {

    private final AccountService service;

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto.Result post(@Valid @RequestBody AccountDto.Create dto) {
        return service.create(dto);
    }

    @GetMapping("accounts/{id}")
    public AccountDto.Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/accounts")
    public List<Result> search(
        @RequestParam(required = false) Long postId,
        @RequestParam(required = false) Long commentId,
        @RequestParam(required = false) Long replyId,
        Pageable pageable
    ) {
        Specification<Account> spec = null;
        if (postId != null) {
            spec = AccountSpecs.hasPost(postId).and(spec);
        }
        if (commentId != null) {
            spec = AccountSpecs.hasComment(commentId).and(spec);
        }
        if (replyId != null) {
            spec = AccountSpecs.hasReply(replyId).and(spec);
        }

        return service.search(spec, pageable);
    }

    @DeleteMapping("/accounts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }

    @PatchMapping("/accounts/{id}")
    public AccountDto.Result patch(@PathVariable Long id,
        @Valid @RequestBody AccountDto.Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @GetMapping("/accounts/{email}/exists")
    @ResponseStatus(HttpStatus.OK)
    public AccountDto.CheckDuplicationResult checkDuplicateUsername(@PathVariable String email) {
        return service.checkDuplicateEmail(email);
    }
}
