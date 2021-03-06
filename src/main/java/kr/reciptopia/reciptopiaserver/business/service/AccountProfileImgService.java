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
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountProfileImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.domain.model.UploadFile;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountProfileImgRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.AccountProfileImgRepositoryImpl;
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
public class AccountProfileImgService {

	private final FileStore fileStore;
	private final AccountProfileImgRepository accountProfileImgRepository;
	private final AccountProfileImgRepositoryImpl accountProfileImgRepositoryImpl;
	private final RepositoryHelper repoHelper;
	private final ServiceErrorHelper errorHelper;
	private final UploadFileAuthorizer uploadFileAuthorizer;

	@Transactional
	public Result put(Long ownerId, MultipartFile imgFile,
		Authentication authentication) {
		throwExceptionWhenInvalidMultipartFile(imgFile);

		Account owner = repoHelper.findAccountOrThrow(ownerId);
		uploadFileAuthorizer.requireByOneself(authentication, owner);

		UploadFile uploadFile = fileStore.storeFile(imgFile);

		Optional<AccountProfileImg> optionalAccountProfileImg =
			accountProfileImgRepository.findByOwnerId(ownerId);

		if (optionalAccountProfileImg.isEmpty()) {
			AccountProfileImg newAccountProfileImg = createAccountProfileImg(uploadFile, owner);
			return Result.of(accountProfileImgRepository.save(newAccountProfileImg));
		}

		AccountProfileImg originAccountProfileImg = optionalAccountProfileImg.get();
		deleteAccountProfileImgFile(originAccountProfileImg);

		originAccountProfileImg = updateAccountProfileImg(uploadFile, originAccountProfileImg);
		return Result.of(accountProfileImgRepository.save(originAccountProfileImg));
	}

	public ResponseEntity<Resource> download(Long id, HttpServletRequest request) {
		AccountProfileImg accountProfileImg = repoHelper.findAccountProfileImgOrThrow(id);
		return createResponseEntity(accountProfileImg, request);
	}

	public ResponseEntity<Resource> downloadByOwnerId(Long ownerId, HttpServletRequest request) {
		Optional<AccountProfileImg> optionalAccountProfileImg =
			accountProfileImgRepository.findByOwnerId(ownerId);

		if (optionalAccountProfileImg.isEmpty()) {
			return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(null);
		}

		AccountProfileImg accountProfileImg = optionalAccountProfileImg.get();
		return createResponseEntity(accountProfileImg, request);
	}

	public Bulk.Result search(
		AccountProfileImgSearchCondition searchCondition, Pageable pageable) {
		PageImpl<AccountProfileImg> pageImpl =
			accountProfileImgRepositoryImpl.search(searchCondition, pageable);
		return Bulk.Result.of(pageImpl);
	}

	@Transactional
	public void delete(Long id, Authentication authentication) {
		AccountProfileImg accountProfileImg = repoHelper.findAccountProfileImgOrThrow(id);
		uploadFileAuthorizer.requireUploadFileOwner(authentication, accountProfileImg);

		File file = new File(fileStore.getFullPath(accountProfileImg.getStoreFileName()));
		if (file.exists()) {
			file.delete();
		}

		accountProfileImgRepository.delete(accountProfileImg);
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

	private AccountProfileImg createAccountProfileImg(UploadFile uploadFile, Account owner) {
		return AccountProfileImg.builder()
			.uploadFileName(uploadFile.getUploadFileName())
			.storeFileName(uploadFile.getStoreFileName())
			.owner(owner)
			.build();
	}

	private void deleteAccountProfileImgFile(AccountProfileImg accountProfileImg) {
		File originFile = new File(fileStore.getFullPath(
			accountProfileImg.getStoreFileName())
		);
		if (originFile.exists()) {
			originFile.delete();
		}
	}

	private AccountProfileImg updateAccountProfileImg(UploadFile uploadFile,
		AccountProfileImg originAccountProfileImg) {
		return AccountProfileImg.builder()
			.uploadFileName(uploadFile.getUploadFileName())
			.storeFileName(uploadFile.getStoreFileName())
			.owner(originAccountProfileImg.getOwner())
			.build()
			.withId(originAccountProfileImg.getId());
	}

	private ResponseEntity<Resource> createResponseEntity(
		AccountProfileImg accountProfileImg, HttpServletRequest request) {
		Resource resource;
		String contentType;
		try {
			Path filePath = Paths.get(
				fileStore.getFullPath(accountProfileImg.getStoreFileName()));

			resource = new UrlResource(filePath.toUri());
			contentType = request.getServletContext().getMimeType(
				resource.getFile().getAbsolutePath());
		} catch (Exception e) {
			throw errorHelper.notFound("File Not Found from "
				+ fileStore.getFullPath(accountProfileImg.getStoreFileName()));
		}

		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
	}

}
