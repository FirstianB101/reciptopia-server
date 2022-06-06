package kr.reciptopia.reciptopiaserver.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import kr.reciptopia.reciptopiaserver.business.service.PostImgService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostImgDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.PostImgDto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class PostImgController {

	private final PostImgService service;

	@PostMapping("/post/images")
	@ResponseStatus(HttpStatus.CREATED)
	public Result create(@RequestPart Long postId,
		@RequestPart MultipartFile imgFile, Authentication authentication) {
		return service.create(postId, imgFile, authentication);
	}

	@PostMapping("/post/bulk-images")
	@ResponseStatus(HttpStatus.CREATED)
	public Bulk.Result bulkPut(@RequestPart Long postId,
		@RequestPart List<MultipartFile> imgFiles, Authentication authentication) {
		return service.bulkPut(postId, imgFiles, authentication);
	}

	@GetMapping("/post/images/{id}/download")
	public ResponseEntity<Resource> download(@PathVariable Long id,
		HttpServletRequest request) {
		return service.download(id, request);
	}

	@GetMapping("/post/images")
	public Bulk.Result search(
		@RequestParam(required = false) Long postId, Pageable pageable) {
		PostImgSearchCondition searchCondition = PostImgSearchCondition.builder()
			.postId(postId)
			.build();

		return service.search(searchCondition, pageable);
	}

	@DeleteMapping("/post/images/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication);
	}

}