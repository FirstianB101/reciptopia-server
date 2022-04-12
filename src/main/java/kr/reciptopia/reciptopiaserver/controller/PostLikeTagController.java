package kr.reciptopia.reciptopiaserver.controller;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.PostLikeTagService;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Result;
import lombok.RequiredArgsConstructor;
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
public class PostLikeTagController {

	private final PostLikeTagService service;

	@PostMapping("/post/likeTags")
	@ResponseStatus(HttpStatus.CREATED)
	public Result post(@Valid @RequestBody Create dto, Authentication authentication) {
		return service.create(dto, authentication);
	}

	@GetMapping("/post/likeTags/{id}")
	public Result get(@PathVariable Long id) {
		return service.read(id);
	}

	@DeleteMapping("/post/likeTags/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication);
	}
}