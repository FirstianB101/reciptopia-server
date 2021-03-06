package kr.reciptopia.reciptopiaserver.controller;

import java.util.List;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.CommentLikeTagService;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentLikeTagDto.Result;
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
public class CommentLikeTagController {

	private final CommentLikeTagService service;

	@PostMapping("/post/comment/likeTags")
	@ResponseStatus(HttpStatus.CREATED)
	public Result post(@Valid @RequestBody Create dto, Authentication authentication) {
		return service.create(dto, authentication);
	}

	@GetMapping("/post/comment/likeTags/{id}")
	public Result get(@PathVariable Long id) {
		return service.read(id);
	}

	@GetMapping("/post/comment/likeTags")
	public List<Result> search(Pageable pageable) {
		return service.search(pageable);
	}

	@DeleteMapping("/post/comment/likeTags/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication);
	}
}