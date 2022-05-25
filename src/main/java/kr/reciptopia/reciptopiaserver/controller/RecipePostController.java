package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.RecipePostDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipePostDto.Result;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.RecipePostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class RecipePostController {

    private final RecipePostService service;

    @PostMapping("/recipePosts")
    @ResponseStatus(HttpStatus.CREATED)
    public Result post(@Valid @RequestBody Create dto, Authentication authentication) {
        return service.create(dto, authentication);
    }
}