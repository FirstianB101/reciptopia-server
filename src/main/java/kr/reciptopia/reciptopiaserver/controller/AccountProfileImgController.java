package kr.reciptopia.reciptopiaserver.controller;

import javax.servlet.http.HttpServletRequest;
import kr.reciptopia.reciptopiaserver.business.service.AccountProfileImgService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountProfileImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Result;
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
public class AccountProfileImgController {

	private final AccountProfileImgService service;

	@PostMapping("/account/profileImages")
	@ResponseStatus(HttpStatus.CREATED)
	public Result put(@RequestPart Long ownerId, @RequestPart MultipartFile imgFile,
		Authentication authentication) {
		return service.put(ownerId, imgFile, authentication);
	}

	@GetMapping("/account/profileImages/{id}/download")
	public ResponseEntity<Resource> download(@PathVariable Long id,
		HttpServletRequest request) {
		return service.download(id, request);
	}

	@GetMapping("/account/profileImages/download")
	public ResponseEntity<Resource> downloadByOwnerId(@RequestParam Long ownerId,
		HttpServletRequest request) {
		return service.downloadByOwnerId(ownerId, request);
	}

	@GetMapping("/account/profileImages")
	public Bulk.Result search(
		@RequestParam(required = false) Long ownerId, Pageable pageable) {
		AccountProfileImgSearchCondition searchCondition = AccountProfileImgSearchCondition.builder()
			.ownerId(ownerId)
			.build();

		return service.search(searchCondition, pageable);
	}

	@DeleteMapping("/account/profileImages/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication);
	}

}