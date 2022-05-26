package kr.reciptopia.reciptopiaserver.domain.dto;

import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;

public interface AccountProfileImgDto {

	interface Bulk {

		@With
		record Result(
			Map<Long, AccountProfileImgDto.Result.Upload> accountProfileImgs) {

			@Builder
			public Result(
				@NotEmpty
				@Singular
					Map<Long, AccountProfileImgDto.Result.Upload> accountProfileImgs) {
				this.accountProfileImgs = accountProfileImgs;
			}

			public static Result of(Page<AccountProfileImg> accountProfileImgs) {
				return Result.builder()
					.accountProfileImgs(
						(Map<? extends Long, ? extends AccountProfileImgDto.Result.Upload>)
							accountProfileImgs.stream()
								.map(AccountProfileImgDto.Result.Upload::of)
								.collect(
									Collectors.toMap(
										AccountProfileImgDto.Result.Upload::id,
										result -> result,
										(x, y) -> y,
										LinkedHashMap::new)))
					.build();
			}
		}
	}

	interface Result {

		@With
		record Upload(
			Long id, String uploadFileName, String storeFileName, Long ownerId) {

			@Builder
			public Upload(
				@NotNull
					Long id,

				@NotEmpty
					String uploadFileName,

				@NotEmpty
					String storeFileName,

				@NotNull
					Long ownerId) {
				this.id = id;
				this.uploadFileName = uploadFileName;
				this.storeFileName = storeFileName;
				this.ownerId = ownerId;
			}

			public static Upload of(AccountProfileImg entity) {
				return Upload.builder()
					.id(entity.getId())
					.uploadFileName(entity.getUploadFileName())
					.storeFileName(entity.getStoreFileName())
					.ownerId(entity.getOwner().getId())
					.build();
			}
		}

		record Download(
			Resource resource) {

			@Builder
			public Download {
			}

			public static Download of(AccountProfileImg entity) throws MalformedURLException {
				return Download.builder()
					.resource(new UrlResource("file:"
						+ System.getProperty("user.dir") + "/" + entity.getStoreFileName()))
					.build();
			}
		}

	}

}
