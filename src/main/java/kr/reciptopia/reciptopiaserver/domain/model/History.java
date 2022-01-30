package kr.reciptopia.reciptopiaserver.domain.model;


import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
public class History extends TimeEntity {

    @Id
    @Column(name = "history_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account owner;

    @ElementCollection(fetch = LAZY)
    private Set<String> ingredients;

    private String recipeName;

    @Builder
    public History(Account owner, Set<String> ingredients) {
        setOwner(owner);
        this.ingredients = ingredients;
    }

    @Builder
    public History(Account owner, String recipeName) {
        setOwner(owner);
        this.recipeName = recipeName;
    }

    public void setOwner(Account owner) {
        if (this.owner != owner) {
            if (this.owner != null)
                this.owner.removeHistory(this);
            this.owner = owner;
            if (owner != null) {
                owner.addHistory(this);
            }
        }
    }
}
