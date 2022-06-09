package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;

public interface StepImgDto {

	interface Bulk {

		@With
		record Result(
			Map<Long, StepImgDto.Result> stepImgs) {

			@Builder
			public Result(
				@NotEmpty
				@Singular
					Map<Long, StepImgDto.Result> stepImgs) {
				this.stepImgs = stepImgs;
			}

			public static Bulk.Result of(Page<StepImg> stepImgs) {
				return Bulk.Result.builder()
					.stepImgs(
						(Map<? extends Long, ? extends StepImgDto.Result>)
							stepImgs.stream()
								.map(StepImgDto.Result::of)
								.collect(
									Collectors.toMap(
										StepImgDto.Result::id,
										result -> result,
										(x, y) -> y,
										LinkedHashMap::new)))
					.build();
			}
		}

	}

	@With
	record Result(
		Long id, String uploadFileName, String storeFileName, Long stepId) {

		@Builder
		public Result(
			@NotNull
				Long id,

			@NotEmpty
				String uploadFileName,

			@NotEmpty
				String storeFileName,

			@NotNull
				Long stepId) {
			this.id = id;
			this.uploadFileName = uploadFileName;
			this.storeFileName = storeFileName;
			this.stepId = stepId;
		}

		public static Result of(StepImg entity) {
			return Result.builder()
				.id(entity.getId())
				.uploadFileName(entity.getUploadFileName())
				.storeFileName(entity.getStoreFileName())
				.stepId(entity.getStep().getId())
				.build();
		}
	}

}
