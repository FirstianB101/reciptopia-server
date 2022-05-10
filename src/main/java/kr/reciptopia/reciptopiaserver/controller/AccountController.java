package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.CheckDuplicationResult;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Update;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.AccountService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@AllArgsConstructor
public class AccountController {

    private final AccountService service;

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto) {
        return service.create(dto);
    }

    @GetMapping("accounts/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/accounts")
    public Bulk.Result search(Pageable pageable) {
        return service.search(pageable);
    }

    @DeleteMapping("/accounts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }

    @PatchMapping("/accounts/{id}")
    public Result patch(@PathVariable Long id,
        @Valid @RequestBody Update dto, Authentication authentication) {
        return service.update(id, dto, authentication);
    }

    @GetMapping("/accounts/{email}/exists")
    @ResponseStatus(HttpStatus.OK)
    public CheckDuplicationResult checkDuplicateUsername(@PathVariable String email) {
        return service.checkDuplicateEmail(email);
    }
}
