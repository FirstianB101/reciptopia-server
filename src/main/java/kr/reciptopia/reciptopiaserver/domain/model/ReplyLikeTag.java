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
public class ReplyLikeTag extends LikeTag {

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "reply_id")
	@NotNull
	private Reply reply;

	@Builder
	public ReplyLikeTag(Account owner, Reply reply) {
		setOwner(owner);
		setReply(reply);
	}

	public void setReply(Reply reply) {
		if (this.reply != reply) {
			if (this.reply != null && reply != null)
				this.reply.removeLikeTag(this);
			this.reply = reply;
			if (reply != null) {
				reply.addLikeTag(this);
			}
		}
	}
}
