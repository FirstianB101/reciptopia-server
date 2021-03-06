package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;

public interface PostImgDto {

	interface Bulk {

		@With
		record Result(
			Map<Long, PostImgDto.Result> postImgs) {

			@Builder
			public Result(
				@NotEmpty
				@Singular
					Map<Long, PostImgDto.Result> postImgs) {
				this.postImgs = postImgs;
			}

			public static Bulk.Result of(Page<PostImg> postImgs) {
				return Bulk.Result.builder()
					.postImgs((Map<? extends Long, ? extends PostImgDto.Result>)
						postImgs.stream()
							.map(PostImgDto.Result::of)
							.collect(
								Collectors.toMap(
									PostImgDto.Result::id,
									result -> result,
									(x, y) -> y,
									LinkedHashMap::new)))
					.build();
			}
		}
	}

	@With
	record Result(
		Long id, String uploadFileName, String storeFileName, Long postId) {

		@Builder
		public Result(
			@NotNull
				Long id,

			@NotEmpty
				String uploadFileName,

			@NotEmpty
				String storeFileName,

			@NotEmpty
				Long postId) {
			this.id = id;
			this.uploadFileName = uploadFileName;
			this.storeFileName = storeFileName;
			this.postId = postId;
		}

		public static Result of(PostImg entity) {
			return Result.builder()
				.id(entity.getId())
				.uploadFileName(entity.getUploadFileName())
				.storeFileName(entity.getStoreFileName())
				.postId(entity.getPost().getId())
				.build();
		}
	}

}
