package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Result.Download;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Result.Upload;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Optional;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.UploadFileAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.filestore.FileStore;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountProfileImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountProfileImgDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.domain.model.UploadFile;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountProfileImgRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.AccountProfileImgRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
	public Upload put(Long ownerId, MultipartFile imgFile,
		Authentication authentication) throws Exception {
		throwExceptionWhenInvalidMultipartFile(imgFile);

		Account owner = repoHelper.findAccountOrThrow(ownerId);
		uploadFileAuthorizer.requireByOneself(authentication, owner);

		UploadFile uploadFile = fileStore.storeFile(imgFile);

		Optional<AccountProfileImg> optionalAccountProfileImg =
			accountProfileImgRepository.findByOwnerId(ownerId);

		if (optionalAccountProfileImg.isEmpty()) {
			AccountProfileImg newAccountProfileImg = createAccountProfileImg(uploadFile, owner);
			return Upload.of(accountProfileImgRepository.save(newAccountProfileImg));
		}

		AccountProfileImg originAccountProfileImg = optionalAccountProfileImg.get();
		File originFile = new File(fileStore.getFullPath(
			originAccountProfileImg.getStoreFileName())
		);
		if (originFile.exists()) {
			originFile.delete();
		} else {
			throw new FileNotFoundException();
		}

		originAccountProfileImg = updateAccountProfileImg(uploadFile, originAccountProfileImg);
		return Upload.of(accountProfileImgRepository.save(originAccountProfileImg));
	}

	public Download read(Long id) throws MalformedURLException {
		AccountProfileImg accountProfileImg = repoHelper.findAccountProfileImgOrThrow(id);
		return Download.of(accountProfileImg, fileStore);
	}

	public Bulk.Result.Upload search(
		AccountProfileImgSearchCondition searchCondition, Pageable pageable) {
		PageImpl<AccountProfileImg> pageImpl =
			accountProfileImgRepositoryImpl.search(searchCondition, pageable);
		return Bulk.Result.Upload.of(pageImpl);
	}

	@Transactional
	public void delete(Long id, Authentication authentication)
		throws FileNotFoundException {
		AccountProfileImg accountProfileImg = repoHelper.findAccountProfileImgOrThrow(id);
		uploadFileAuthorizer.requireUploadFileOwner(authentication, accountProfileImg);

		File file = new File(fileStore.getFullPath(accountProfileImg.getStoreFileName()));
		if (file.exists()) {
			file.delete();
		} else {
			throw new FileNotFoundException();
		}

		accountProfileImgRepository.delete(accountProfileImg);
	}

	private void throwExceptionWhenInvalidMultipartFile(MultipartFile multipartFile)
		throws IOException {
		String originalFileName = multipartFile.getOriginalFilename();

		File file = new File(originalFileName);
		String contentType = Files.probeContentType(file.toPath());
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

	private AccountProfileImg updateAccountProfileImg(UploadFile uploadFile,
		AccountProfileImg originAccountProfileImg) {
		return AccountProfileImg.builder()
			.uploadFileName(uploadFile.getUploadFileName())
			.storeFileName(uploadFile.getStoreFileName())
			.owner(originAccountProfileImg.getOwner())
			.build()
			.withId(originAccountProfileImg.getId());
	}

}
