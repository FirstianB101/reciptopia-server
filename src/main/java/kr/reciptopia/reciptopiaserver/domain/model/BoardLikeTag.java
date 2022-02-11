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
public class BoardLikeTag extends LikeTag {
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "board_id")
	private Board board;

	@Builder
	public BoardLikeTag(Account owner, Board board) {
		setOwner(owner);
		setBoard(board);
	}

	public void setBoard(Board board) {
		if (this.board != board) {
			if (this.board != null)
				this.board.removeLikeTag(this);
			this.board = board;
			if (board != null) {
				board.addLikeTag(this);
			}
		}
	}
}
