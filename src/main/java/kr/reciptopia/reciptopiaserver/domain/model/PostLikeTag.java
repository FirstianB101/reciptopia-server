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

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@Entity
public class PostLikeTag extends LikeTag {

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "post_id")
	@NotNull
	private Post post;

	@Builder
	public PostLikeTag(Account owner, Post post) {
		super(owner);
		setPost(post);
	}

	public PostLikeTag withId(Long id) {
		PostLikeTag postLikeTag = PostLikeTag.builder()
			.owner(owner)
			.post(post)
			.build();
		postLikeTag.setId(id);
		return this.id != null && this.id.equals(id) ? this : postLikeTag;
	}

	public PostLikeTag withOwner(Account owner) {
		return this.owner != null && this.owner.equals(owner) ? this :
			PostLikeTag.builder()
				.owner(owner)
				.post(post)
				.build()
				.withId(id);
	}

	public PostLikeTag withPost(Post post) {
		return this.post != null && this.post.equals(post) ? this :
			PostLikeTag.builder()
				.owner(owner)
				.post(post)
				.build()
				.withId(id);
	}

	public void setPost(Post post) {
		if (this.post != post) {
			if (this.post != null && post != null)
				this.post.removeLikeTag(this);
			this.post = post;
			if (post != null) {
				post.addLikeTag(this);
			}
		}
	}
}
