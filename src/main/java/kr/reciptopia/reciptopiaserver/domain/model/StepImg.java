package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
@Getter
@ToString
@Entity
public class StepImg extends UploadFile {

	@ToString.Exclude
	@OneToOne(fetch = LAZY)
	@JoinColumn(name = "step_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@NotNull
	private Step step;

	@Builder
	public StepImg(String uploadFileName, String storeFileName, Step step) {
		super(uploadFileName, storeFileName);
		setStep(step);
	}

	public StepImg withId(Long id) {
		StepImg stepImg = StepImg.builder()
			.uploadFileName(uploadFileName)
			.storeFileName(storeFileName)
			.step(step)
			.build();
		stepImg.setId(id);
		return this.id != null && this.id.equals(id) ? this : stepImg;
	}

	public StepImg withUploadFileName(String uploadFileName) {
		return this.uploadFileName != null && this.uploadFileName.equals(uploadFileName) ? this :
			StepImg.builder()
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.step(step)
				.build()
				.withId(id);
	}

	public StepImg withStoreFileName(String storeFileName) {
		return this.storeFileName != null && this.storeFileName.equals(storeFileName) ? this :
			StepImg.builder()
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.step(step)
				.build()
				.withId(id);
	}

	public StepImg withStep(Step step) {
		return this.step != null && this.step.equals(step) ? this :
			StepImg.builder()
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.step(step)
				.build()
				.withId(id);
	}

}
