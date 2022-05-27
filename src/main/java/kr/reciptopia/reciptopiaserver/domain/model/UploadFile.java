package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Setter
public class UploadFile extends TimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "upload_file_id")
	protected Long id;

	@NotEmpty
	protected String uploadFileName;

	@NotEmpty
	protected String storeFileName;

	public UploadFile(String uploadFileName, String storeFileName) {
		setUploadFileName(uploadFileName);
		setStoreFileName(storeFileName);
	}
}
