package kr.reciptopia.reciptopiaserver.business.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.UploadFileAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.filestore.FileStore;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.StepImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.StepImgDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.StepImgDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import kr.reciptopia.reciptopiaserver.domain.model.UploadFile;
import kr.reciptopia.reciptopiaserver.persistence.repository.StepImgRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.StepImgRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StepImgService {

	private final FileStore fileStore;
	private final StepImgRepository stepImgRepository;
	private final StepImgRepositoryImpl stepImgRepositoryImpl;
	private final RepositoryHelper repoHelper;
	private final ServiceErrorHelper errorHelper;
	private final UploadFileAuthorizer uploadFileAuthorizer;

	@Transactional
	public Result put(Long stepId, MultipartFile imgFile,
		Authentication authentication) {
		throwExceptionWhenInvalidMultipartFile(imgFile);

		Step step = repoHelper.findStepOrThrow(stepId);
		uploadFileAuthorizer.requireByOneself(authentication,
			step.getRecipe().getPost().getOwner());

		UploadFile uploadFile = fileStore.storeFile(imgFile);

		Optional<StepImg> optionalStepImg = stepImgRepository.findByStepId(stepId);

		if (optionalStepImg.isEmpty()) {
			StepImg newStepImg = createStepImg(uploadFile, step);
			return Result.of(stepImgRepository.save(newStepImg));
		}

		StepImg originStepImg = optionalStepImg.get();
		deleteStepImgFile(originStepImg);

		originStepImg = updateStepImg(uploadFile, originStepImg);
		return Result.of(stepImgRepository.save(originStepImg));
	}

	public ResponseEntity<Resource> download(Long id, HttpServletRequest request) {
		StepImg stepImg = repoHelper.findStepImgOrThrow(id);
		return createResponseEntity(stepImg, request);
	}

	public ResponseEntity<Resource> downloadByStepId(Long stepId, HttpServletRequest request) {
		Optional<StepImg> optionalStepImg = stepImgRepository.findByStepId(stepId);

		if (optionalStepImg.isEmpty()) {
			return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(null);
		}

		StepImg stepImg = optionalStepImg.get();
		return createResponseEntity(stepImg, request);
	}

	public Bulk.Result search(
		StepImgSearchCondition searchCondition, Pageable pageable) {
		PageImpl<StepImg> pageImpl =
			stepImgRepositoryImpl.search(searchCondition, pageable);
		return Bulk.Result.of(pageImpl);
	}

	@Transactional
	public void delete(Long id, Authentication authentication) {
		StepImg stepImg = repoHelper.findStepImgOrThrow(id);
		uploadFileAuthorizer.requireUploadFileOwner(authentication, stepImg);

		File file = new File(fileStore.getFullPath(stepImg.getStoreFileName()));
		if (file.exists()) {
			file.delete();
		}

		stepImgRepository.delete(stepImg);
	}

	private void throwExceptionWhenInvalidMultipartFile(MultipartFile multipartFile) {
		String originalFileName = multipartFile.getOriginalFilename();

		File file = new File(originalFileName);
		String contentType;
		try {
			contentType = Files.probeContentType(file.toPath());
		} catch (Exception ex) {
			throw errorHelper.notFound("File Not Found from" + file.toPath());
		}

		if (multipartFile.isEmpty() || !contentType.startsWith("image")) {
			throw errorHelper.badRequest("Requested MultipartFile is invalid");
		}
	}

	private StepImg createStepImg(UploadFile uploadFile, Step step) {
		return StepImg.builder()
			.uploadFileName(uploadFile.getUploadFileName())
			.storeFileName(uploadFile.getStoreFileName())
			.step(step)
			.build();
	}

	private void deleteStepImgFile(StepImg stepImg) {
		File originFile = new File(fileStore.getFullPath(
			stepImg.getStoreFileName()
		));
		if (originFile.exists()) {
			originFile.delete();
		}
	}

	private StepImg updateStepImg(UploadFile uploadFile, StepImg originStepImg) {
		return StepImg.builder()
			.uploadFileName(uploadFile.getUploadFileName())
			.storeFileName(uploadFile.getStoreFileName())
			.step(originStepImg.getStep())
			.build()
			.withId(originStepImg.getId());
	}

	private ResponseEntity<Resource> createResponseEntity(
		StepImg stepImg, HttpServletRequest request) {
		Resource resource;
		String contentType;
		try {
			Path filePath = Paths.get(
				fileStore.getFullPath(stepImg.getStoreFileName()));

			resource = new UrlResource(filePath.toUri());
			contentType = request.getServletContext().getMimeType(
				resource.getFile().getAbsolutePath());
		} catch (Exception e) {
			throw errorHelper.notFound("File Not Found from "
				+ fileStore.getFullPath(stepImg.getStoreFileName()));
		}

		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
	}

}
