package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Bulk;

import java.util.List;
import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.PostLikeTagService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostLikeTagSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Result;
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

	@GetMapping("/post/likeTags")
	public Bulk.Result search(
		@RequestParam(required = false) Long id,
		@RequestParam(required = false) List<Long> ids,
		@RequestParam(required = false) Long ownerId,
		@RequestParam(required = false) List<Long> ownerIds,
		Pageable pageable) {
		PostLikeTagSearchCondition postLikeTagSearchCondition = PostLikeTagSearchCondition.builder()
			.id(id)
			.ids(ids)
			.ownerIds(ownerIds)
			.ownerId(ownerId)
			.build();

		return service.search(postLikeTagSearchCondition, pageable);
	}

	@DeleteMapping("/post/likeTags/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication);
	}
}