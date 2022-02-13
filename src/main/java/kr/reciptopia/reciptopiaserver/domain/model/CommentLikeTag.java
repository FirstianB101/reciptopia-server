package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

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
	@NotNull
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
