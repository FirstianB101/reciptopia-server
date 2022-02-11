package kr.reciptopia.reciptopiaserver.domain.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Setter
public abstract class LikeTag {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "like_tag_id")
	private Long id;

	@ToString.Exclude
	@NotNull
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "account_id")
	private Account owner;

	public void setOwner(Account owner) {
		if (this.owner != owner) {
			if (this.owner != null)
				this.owner.removeLikeTag(this);
			this.owner = owner;
			if (owner != null) {
				owner.addLikeTag(this);
			}
		}
	}
}
