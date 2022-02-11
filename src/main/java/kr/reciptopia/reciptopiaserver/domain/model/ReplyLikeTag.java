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
public class ReplyLikeTag extends LikeTag {
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "reply_id")
	private Reply reply;

	@Builder
	public ReplyLikeTag(Account owner, Reply reply) {
		setOwner(owner);
		setReply(reply);
	}

	public void setReply(Reply reply) {
		if (this.reply != reply) {
			if (this.reply != null)
				this.reply.removeLikeTag(this);
			this.reply = reply;
			if (reply != null) {
				reply.addLikeTag(this);
			}
		}
	}
}
