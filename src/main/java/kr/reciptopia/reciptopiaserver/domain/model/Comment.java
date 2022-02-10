package kr.reciptopia.reciptopiaserver.domain.model;

import kr.reciptopia.reciptopiaserver.domain.error.exception.LikeTagNotFoundException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.ReplyNotFoundException;
import lombok.*;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Comment extends TimeEntity {

	@Id
	@Column(name = "comment_id")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "account_id")
	private Account owner;

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	@NotBlank
	@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
	private String content;

	@NotNull
	@ToString.Exclude
	@OneToMany(mappedBy = "comment", cascade = ALL, orphanRemoval = true)
	private Set<Reply> replies = new HashSet<>();

	@NotNull
	@ToString.Exclude
	@OneToMany(mappedBy = "comment", cascade = ALL, orphanRemoval = true)
	private Set<LikeTag> likes = new HashSet<>();

	@Builder
	public Comment(Account owner, Board board, String content) {
		setOwner(owner);
		setBoard(board);
		setContent(content);
	}


	public void addLikeTag(LikeTag likeTag) {
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

	public void addReply(Reply reply) {
		replies.add(reply);
		if (!this.equals(reply.getOwner())) {
			reply.setOwner(this);
		}
	}

	public void removeReply(Reply reply) {
		if (!replies.contains(reply))
			throw new ReplyNotFoundException();

		replies.remove(reply);
		if (this.equals(reply.getOwner())) {
			reply.setOwner(null);
		}
	}
}