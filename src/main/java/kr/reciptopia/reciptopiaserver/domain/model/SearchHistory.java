package kr.reciptopia.reciptopiaserver.domain.model;


import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
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
import lombok.Singular;
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
public class SearchHistory extends TimeEntity {

    @Id
    @Column(name = "search_history_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id")
    private Account owner;

    @Singular
    @ElementCollection(fetch = LAZY)
    @JoinColumn(name = "search_history_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<String> ingredientNames = new HashSet<>();

    @Builder
    public SearchHistory(Account owner, @Singular Set<String> ingredientNames) {
        setOwner(owner);
        this.ingredientNames = ingredientNames;
    }

    public SearchHistory withIngredientName(String ingredientName) {
        return SearchHistory.builder()
            .ingredientNames(ingredientNames)
            .ingredientName(ingredientName)
            .owner(owner)
            .build()
            .withId(id);
    }
}
