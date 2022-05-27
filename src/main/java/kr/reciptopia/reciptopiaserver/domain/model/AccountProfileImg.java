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
public class AccountProfileImg extends UploadFile {

	@ToString.Exclude
	@OneToOne(fetch = LAZY)
	@JoinColumn(name = "account_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@NotNull
	private Account owner;

	@Builder
	public AccountProfileImg(String uploadFileName, String storeFileName, Account owner) {
		super(uploadFileName, storeFileName);
		setOwner(owner);
	}

	public AccountProfileImg withId(Long id) {
		AccountProfileImg accountProfileImg = AccountProfileImg.builder()
			.uploadFileName(uploadFileName)
			.storeFileName(storeFileName)
			.owner(owner)
			.build();
		accountProfileImg.setId(id);
		return this.id != null && this.id.equals(id) ? this : accountProfileImg;
	}

	public AccountProfileImg withUploadFileName(String uploadFileName) {
		return this.uploadFileName != null && this.uploadFileName.equals(uploadFileName) ? this :
			AccountProfileImg.builder()
				.owner(owner)
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.build()
				.withId(id);
	}

	public AccountProfileImg withStoreFileName(String storeFileName) {
		return this.storeFileName != null && this.storeFileName.equals(storeFileName) ? this :
			AccountProfileImg.builder()
				.owner(owner)
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.build()
				.withId(id);
	}

	public AccountProfileImg withOwner(Account owner) {
		return this.owner != null && this.owner.equals(owner) ? this :
			AccountProfileImg.builder()
				.owner(owner)
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.build()
				.withId(id);
	}

}


