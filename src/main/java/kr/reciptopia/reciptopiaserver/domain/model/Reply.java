package kr.reciptopia.reciptopiaserver.domain.model;

import kr.reciptopia.reciptopiaserver.domain.error.exception.LikeTagNotFoundException;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Reply {

	@Id
	@Column(name = "reply_id")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "account_id")
	private Account owner;

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@NotBlank
	@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
	private String content;

	@NotNull
	@ToString.Exclude
	@OneToMany(mappedBy = "reply", cascade = ALL, orphanRemoval = true)
	private Set<LikeTag> likes = new HashSet<>();

	@Builder
	public Reply(Account owner, Comment comment, String content) {
		setOwner(owner);
		setComment(comment);
		setContent(content);
	}

	public void addLikeTag(LikeTag liketag) {
		likes.add(likeTag);
		if (!this.equals(likeTag.getOwner())) {
			likeTag.setOwner(this);
		}
	}

	public void removeLikeTag(LikeTag likeTag) {
		if (!likes.contains(likeTag))
			throw new LikeTagNotFoundException();

		likes.remove(likeTag);
		if (this.equals(likeTag.getOwner())) {
			likeTag.setOwner(null);
		}
	}
}
