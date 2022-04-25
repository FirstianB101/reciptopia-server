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
@Getter
@Setter
@ToString
@Entity
public class ReplyLikeTag extends LikeTag {

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "reply_id")
	@NotNull
	private Reply reply;

	@Builder
	public ReplyLikeTag(Account owner, Reply reply) {
		super(owner);
		setReply(reply);
	}

	public ReplyLikeTag withId(Long id) {
		ReplyLikeTag replyLikeTag = ReplyLikeTag.builder()
			.owner(owner)
			.reply(reply)
			.build();
		replyLikeTag.setId(id);
		return this.id != null && this.id.equals(id) ? this : replyLikeTag;
	}

	public ReplyLikeTag withOwner(Account owner) {
		return this.owner != null && this.owner.equals(owner) ? this :
			ReplyLikeTag.builder()
				.owner(owner)
				.reply(reply)
				.build()
				.withId(id);
	}

	public ReplyLikeTag withReply(Reply reply) {
		return this.reply != null && this.reply.equals(reply) ? this :
			ReplyLikeTag.builder()
				.owner(owner)
				.reply(reply)
				.build()
				.withId(id);
	}
}
