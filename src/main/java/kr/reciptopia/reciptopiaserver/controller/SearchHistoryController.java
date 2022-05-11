package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto.Result;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.SearchHistoryService;
import kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService service;

    @PostMapping("/account/searchHistories")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto,
        Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/account/searchHistories/{id}")
    public Result get(@PathVariable Long id, Authentication authentication) {
        return service.read(id, authentication);
    }

    @GetMapping("/account/{ownerId}/searchHistories")
    public SearchHistoryDto.Bulk.Result search(
        @PathVariable Long ownerId,
        Authentication authentication,
        Pageable pageable
    ) {
        return service.search(ownerId, authentication, pageable);
    }

    @DeleteMapping("/account/searchHistories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }
}