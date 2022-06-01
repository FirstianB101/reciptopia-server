package kr.reciptopia.reciptopiaserver.business.service.filestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.UploadFile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class FileStore {

	private final ServiceErrorHelper errorHelper;

	@Value("${file.upload.location}")
	private String fileDir;

	public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) {
		List<UploadFile> storeFileResult = new ArrayList<>();
		for (MultipartFile multipartFile : multipartFiles) {
			if (!multipartFile.isEmpty()) {
				storeFileResult.add(storeFile(multipartFile));
			}
		}

		return storeFileResult;
	}

	public UploadFile storeFile(MultipartFile multipartFile) {
		String originalFileName = multipartFile.getOriginalFilename();
		String storeFileName = createStoreFileName(originalFileName);

		try {
			multipartFile.transferTo(new File(getFullPath(storeFileName)));
		} catch (Exception ex) {
			throw errorHelper.notFound("File Not Found from "
				+ getFullPath(storeFileName));
		}
		return new UploadFile(originalFileName, storeFileName);
	}

	public String getFullPath(String fileName) {
		return fileDir + fileName;
	}

	private String createStoreFileName(String originalFileName) {
		String ext = extractExt(originalFileName);
		String uuid = UUID.randomUUID().toString();
		return uuid + "." + ext;
	}

	private String extractExt(String originalFileName) {
		int pos = originalFileName.lastIndexOf(".");
		return originalFileName.substring(pos + 1);
	}

}
