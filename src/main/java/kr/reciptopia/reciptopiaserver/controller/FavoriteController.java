package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.Result;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.FavoriteService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.FavoriteSearchCondition;
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
public class FavoriteController {

    private final FavoriteService service;

    @PostMapping("/account/favorites")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto,
        Authentication authentication) {
        return service.create(dto, authentication);
    }

    @GetMapping("/account/favorites/{id}")
    public Result get(@PathVariable Long id) {
        return service.read(id);
    }

    @GetMapping("/account/favorites")
    public Bulk.Result search(
        @RequestParam(required = false) Long ownerId,
        Pageable pageable) {
        FavoriteSearchCondition favoriteSearchCondition = FavoriteSearchCondition.builder()
            .ownerId(ownerId)
            .build();

        return service.search(favoriteSearchCondition, pageable);
    }

    @DeleteMapping("/account/favorites/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication);
    }
}