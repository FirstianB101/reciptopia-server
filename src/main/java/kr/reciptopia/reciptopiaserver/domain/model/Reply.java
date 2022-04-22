package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Reply extends TimeEntity {

	@Id
	@Column(name = "reply_id")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "account_id")
	private Account owner;

	@ToString.Exclude
	@ManyToOne(fetch = LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@NotBlank
	@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
	private String content;

	@Builder
	public Reply(Account owner, Comment comment, String content) {
		setOwner(owner);
		setComment(comment);
		setContent(content);
	}
}
