package kr.reciptopia.reciptopiaserver.controller;

import java.util.List;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.ReplyLikeTagService;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyLikeTagDto.Result;
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
public class ReplyLikeTagController {

	private final ReplyLikeTagService service;

	@PostMapping("/post/comment/reply/likeTags")
	@ResponseStatus(HttpStatus.CREATED)
	public Result post(@Valid @RequestBody Create dto, Authentication authentication) {
		return service.create(dto, authentication);
	}

	@GetMapping("/post/comment/reply/likeTags/{id}")
	public Result get(@PathVariable Long id) {
		return service.read(id);
	}

	@GetMapping("/post/comment/reply/likeTags")
	public List<Result> search(Pageable pageable) {
		return service.search(pageable);
	}

	@DeleteMapping("/post/comment/reply/likeTags/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication);
	}
}