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
	private Set<ReplyLikeTag> replyLikeTags = new HashSet<>();

	@Builder
	public Reply(Account owner, Comment comment, String content) {
		setOwner(owner);
		setComment(comment);
		setContent(content);
	}

	public void setOwner(Account owner) {
		if (this.owner != owner) {
			if (this.owner != null)
				this.owner.removeReply(this);
			this.owner = owner;
			if (owner != null) {
				owner.addReply(this);
			}
		}
	}

	public void setComment(Comment comment) {
		if (this.comment != comment) {
			if (this.comment != null)
				this.comment.removeReply(this);
			this.comment = comment;
			if (comment != null) {
				comment.addReply(this);
			}
		}
	}

	public void addLikeTag(ReplyLikeTag likeTag) {
		replyLikeTags.add(likeTag);
		if (!this.equals(likeTag.getReply())) {
			likeTag.setReply(this);
		}
	}

	public void removeLikeTag(ReplyLikeTag likeTag) {
		if (!replyLikeTags.contains(likeTag))
			throw new LikeTagNotFoundException();

		replyLikeTags.remove(likeTag);
		if (this.equals(likeTag.getReply())) {
			likeTag.setReply(null);
		}
	}
}
