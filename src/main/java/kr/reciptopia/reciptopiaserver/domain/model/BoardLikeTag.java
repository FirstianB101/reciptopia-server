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
@NotNull
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
