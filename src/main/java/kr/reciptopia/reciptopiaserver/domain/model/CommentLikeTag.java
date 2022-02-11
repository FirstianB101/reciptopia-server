package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class CommentLikeTag extends LikeTag {
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@Builder
	public CommentLikeTag(Account owner, Comment comment) {
		setOwner(owner);
		setComment(comment);
	}

	public void setComment(Comment comment) {
		if (this.comment != comment) {
			if (this.comment != null)
				this.comment.removeLikeTag(this);
			this.comment = comment;
			if (comment != null) {
				comment.addLikeTag(this);
			}
		}
	}
}
