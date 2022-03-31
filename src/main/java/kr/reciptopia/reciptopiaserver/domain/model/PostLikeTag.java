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
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class PostLikeTag extends LikeTag {

	@ToString.Exclude
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
