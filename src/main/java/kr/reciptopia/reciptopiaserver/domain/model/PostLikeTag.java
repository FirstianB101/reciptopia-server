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
public class PostLikeTag extends LikeTag {
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "post_id")
	@NotNull
	private Post post;

	@Builder
	public PostLikeTag(Account owner, Post post) {
		setOwner(owner);
		setPost(post);
	}

	public void setPost(Post post) {
		if (this.post != post) {
			if (this.post != null)
				this.post.removeLikeTag(this);
			this.post = post;
			if (post != null) {
				post.addLikeTag(this);
			}
		}
	}
}
