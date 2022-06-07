package kr.reciptopia.reciptopiaserver.controller;

import javax.servlet.http.HttpServletRequest;
import kr.reciptopia.reciptopiaserver.business.service.StepImgService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.StepImgDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.StepImgDto.Result;
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
public class StepImgController {

	private final StepImgService service;

	@PostMapping("/post/recipe/step/images")
	@ResponseStatus(HttpStatus.CREATED)
	public Result put(@RequestPart Long stepId, @RequestPart MultipartFile imgFile,
		Authentication authentication) {
		return service.put(stepId, imgFile, authentication);
	}

	@GetMapping("/post/recipe/step/images/{id}/download")
	public ResponseEntity<Resource> download(@PathVariable Long id,
		HttpServletRequest request) {
		return service.download(id, request);
	}

	@GetMapping("/post/recipe/step/images/download")
	public ResponseEntity<Resource> downloadByStepId(@RequestParam Long stepId,
		HttpServletRequest request) {
		return service.downloadByStepId(stepId, request);
	}

	@GetMapping("/post/recipe/step/images")
	public Bulk.Result search(
		@RequestParam(required = false) Long stepId,
		@RequestParam(required = false) Long recipeId, Pageable pageable) {
		StepImgSearchCondition searchCondition = StepImgSearchCondition.builder()
			.stepId(stepId)
			.recipeId(recipeId)
			.build();

		return service.search(searchCondition, pageable);
	}

	@DeleteMapping("/post/recipe/step/images/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication);
	}

}