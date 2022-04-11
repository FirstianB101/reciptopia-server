package kr.reciptopia.reciptopiaserver.domain.model;


import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class SearchHistory extends TimeEntity {

    @Id
    @Column(name = "search_history_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account owner;

    @ElementCollection(fetch = LAZY)
    private Set<String> ingredients = new HashSet<>();

    private String recipeName;

    @Builder
    public SearchHistory(Account owner, Set<String> ingredients) {
        setOwner(owner);
        this.ingredients = ingredients;
    }

    @Builder
    public SearchHistory(Account owner, String recipeName) {
        setOwner(owner);
        this.recipeName = recipeName;
    }

    public void setOwner(Account owner) {
        if (this.owner != owner) {
            if (this.owner != null && owner != null)
                this.owner.removeSearchHistory(this);
            this.owner = owner;
            if (owner != null) {
                owner.addSearchHistory(this);
            }
        }
    }
}
