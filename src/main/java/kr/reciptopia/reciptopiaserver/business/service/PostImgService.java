package kr.reciptopia.reciptopiaserver.business.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.UploadFileAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.filestore.FileStore;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.PostImgDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.PostImgDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import kr.reciptopia.reciptopiaserver.domain.model.UploadFile;
import kr.reciptopia.reciptopiaserver.persistence.repository.PostImgRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.PostImgRepositoryImpl;
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
public class PostImgService {

	private final FileStore fileStore;
	private final PostImgRepository postImgRepository;
	private final PostImgRepositoryImpl postImgRepositoryImpl;
	private final RepositoryHelper repoHelper;
	private final ServiceErrorHelper errorHelper;
	private final UploadFileAuthorizer uploadFileAuthorizer;

	@Transactional
	public Result create(Long postId, MultipartFile imgFile,
		Authentication authentication) {
		throwExceptionWhenInvalidMultipartFile(imgFile);

		Post post = repoHelper.findPostOrThrow(postId);
		uploadFileAuthorizer.requireByOneself(authentication, post.getOwner());

		UploadFile uploadFile = fileStore.storeFile(imgFile);
		PostImg postImg = createPostImg(uploadFile, post);

		return Result.of(postImgRepository.save(postImg));
	}

	@Transactional
	public Bulk.Result bulkPut(Long postId, List<MultipartFile> imgFiles,
		Authentication authentication) {
		Post post = repoHelper.findPostOrThrow(postId);
		uploadFileAuthorizer.requireByOneself(authentication, post.getOwner());

		clearByPostId(post.getId());

		Map<Long, Result> postImgs = new LinkedHashMap<>();
		for (MultipartFile imgFile : imgFiles) {
			throwExceptionWhenInvalidMultipartFile(imgFile);

			UploadFile uploadFile = fileStore.storeFile(imgFile);
			PostImg postImg = createPostImg(uploadFile, post);

			Result uploadDto = Result.of(postImgRepository.save(postImg));
			postImgs.put(postImg.getId(), uploadDto);
		}

		return Bulk.Result.builder()
			.postImgs(postImgs)
			.build();
	}

	public ResponseEntity<Resource> download(Long id, HttpServletRequest request) {
		PostImg postImg = repoHelper.findPostImgOrThrow(id);
		return createResponseEntity(postImg, request);
	}

	public Bulk.Result search(
		PostImgSearchCondition searchCondition, Pageable pageable) {
		PageImpl<PostImg> pageImpl =
			postImgRepositoryImpl.search(searchCondition, pageable);
		return Bulk.Result.of(pageImpl);
	}

	@Transactional
	public void delete(Long id, Authentication authentication) {
		PostImg postImg = repoHelper.findPostImgOrThrow(id);
		uploadFileAuthorizer.requireUploadFileOwner(authentication, postImg);

		File file = new File(fileStore.getFullPath(postImg.getStoreFileName()));
		if (file.exists()) {
			file.delete();
		}

		postImgRepository.delete(postImg);
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

	private PostImg createPostImg(UploadFile uploadFile, Post post) {
		return PostImg.builder()
			.uploadFileName(uploadFile.getUploadFileName())
			.storeFileName(uploadFile.getStoreFileName())
			.post(post)
			.build();
	}

	private void clearByPostId(Long postId) {
		List<PostImg> postImgList = postImgRepository.findAllByPostId(postId);
		for (PostImg postImg : postImgList) {
			File file = new File(fileStore.getFullPath(postImg.getStoreFileName()));
			if (file.exists()) {
				file.delete();
			}
		}

		postImgRepository.deleteAllInBatch(postImgList);
	}

	private ResponseEntity<Resource> createResponseEntity(
		PostImg postImg, HttpServletRequest request) {
		Resource resource;
		String contentType;

		try {
			Path filePath = Paths.get(
				fileStore.getFullPath(postImg.getStoreFileName()));

			resource = new UrlResource(filePath.toUri());
			contentType = request.getServletContext().getMimeType(
				resource.getFile().getAbsolutePath());
		} catch (Exception e) {
			throw errorHelper.notFound("File Not Found from "
				+ fileStore.getFullPath(postImg.getStoreFileName()));
		}

		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + resource.getFilename() + "\"")
			.body(resource);
	}

}
