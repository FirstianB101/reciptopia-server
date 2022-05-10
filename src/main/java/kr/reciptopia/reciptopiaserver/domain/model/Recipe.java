package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.error.exception.IllegalTypeIngredientException;
import kr.reciptopia.reciptopiaserver.domain.error.exception.IngredientNotFoundException;
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
public class Recipe extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "recipe_id")
    private Long id;

    @NotNull
    @ToString.Exclude
    @OneToOne(fetch = LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id")
    private Post post;

    @NotNull
    @Singular
    @ToString.Exclude
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<MainIngredient> mainIngredients = new HashSet<>();

    @NotNull
    @Singular
    @ToString.Exclude
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<SubIngredient> subIngredients = new HashSet<>();

    @Builder
    public Recipe(Post post) {
        setPost(post);
    }

    public <I extends Ingredient> void addIngredient(I ingredient) {
        if (ingredient instanceof MainIngredient)
            mainIngredients.add((MainIngredient) ingredient);
        else if (ingredient instanceof SubIngredient)
            subIngredients.add((SubIngredient) ingredient);
        else
            throw new IllegalTypeIngredientException();

        if (!this.equals(ingredient.getRecipe())) {
            ingredient.setRecipe(this);
        }
        Recipe.builder()
            .post(post)
            .build()
            .withMainIngredients(mainIngredients)
            .withSubIngredients(subIngredients)
            .withId(id);
    }

    public <I extends Ingredient> void removeIngredient(I ingredient) {
        Set<? extends Ingredient> ingredients;
        if (ingredient instanceof MainIngredient) {
            ingredients = mainIngredients;
        } else if (ingredient instanceof SubIngredient)
            ingredients = subIngredients;
        else
            throw new IllegalTypeIngredientException();

        if (!ingredients.contains(ingredient))
            throw new IngredientNotFoundException();

        ingredients.remove(ingredient);
        if (this.equals(ingredient.getRecipe())) {
            ingredient.setRecipe(null);
        }
    }

}
