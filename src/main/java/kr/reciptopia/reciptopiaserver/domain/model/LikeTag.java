package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Setter
public abstract class LikeTag extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "like_tag_id")
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account owner;

    public void setOwner(Account owner) {
        if (this.owner != owner) {
            if (this.owner != null && owner != null)
                this.owner.removeLikeTag(this);
            this.owner = owner;
            if (owner != null) {
                owner.addLikeTag(this);
            }
        }
    }
}
