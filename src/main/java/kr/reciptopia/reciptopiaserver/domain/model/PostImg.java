package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
public class PostImg extends UploadFile {

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "post_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@NotNull
	private Post post;

	@Builder
	public PostImg(String uploadFileName, String storeFileName, Post post) {
		super(uploadFileName, storeFileName);
		setPost(post);
	}

	public PostImg withId(Long id) {
		PostImg postImg = PostImg.builder()
			.uploadFileName(uploadFileName)
			.storeFileName(storeFileName)
			.post(post)
			.build();
		postImg.setId(id);
		return this.id != null && this.id.equals(id) ? this : postImg;
	}

	public PostImg withUploadFileName(String uploadFileName) {
		return this.uploadFileName != null && this.uploadFileName.equals(uploadFileName) ? this :
			PostImg.builder()
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.post(post)
				.build()
				.withId(id);
	}

	public PostImg withStoreFileName(String storeFileName) {
		return this.storeFileName != null && this.storeFileName.equals(storeFileName) ? this :
			PostImg.builder()
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.post(post)
				.build()
				.withId(id);
	}

	public PostImg withPost(Post post) {
		return this.post != null && this.post.equals(post) ? this :
			PostImg.builder()
				.uploadFileName(uploadFileName)
				.storeFileName(storeFileName)
				.post(post)
				.build()
				.withId(id);
	}

}
