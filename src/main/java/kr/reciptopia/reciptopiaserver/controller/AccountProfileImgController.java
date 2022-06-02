package kr.reciptopia.reciptopiaserver.controller;

import static kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Result.Upload;
import javax.servlet.http.HttpServletRequest;
import kr.reciptopia.reciptopiaserver.business.service.AccountProfileImgService;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountProfileImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Bulk;
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
	public Upload put(@RequestPart Long ownerId, @RequestPart MultipartFile imgFile,
		Authentication authentication) {
		return service.put(ownerId, imgFile, authentication);
	}

	@GetMapping("/account/profileImages/{id}")
	public ResponseEntity<Resource> get(@PathVariable Long id,
		HttpServletRequest request) {
		return service.read(id, request);
	}

	@GetMapping("/account/profileImages")
	public Bulk.Result.Upload search(
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