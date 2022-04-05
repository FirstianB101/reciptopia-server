package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import kr.reciptopia.reciptopiaserver.domain.error.exception.StepNotFoundException;
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
public class Recipe extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "recipe_id")
    private Long id;

    @NotNull
    @ToString.Exclude
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "recipe", cascade = ALL, orphanRemoval = true)
    private List<Step> steps = new ArrayList<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "recipe", cascade = ALL, orphanRemoval = true)
    private Set<MainIngredient> mainIngredients = new HashSet<>();

    @NotNull
    @ToString.Exclude
    @OneToMany(mappedBy = "recipe", cascade = ALL, orphanRemoval = true)
    private Set<SubIngredient> subIngredients = new HashSet<>();

    @Builder
    public Recipe(Post post, @Singular Set<Step> steps,
        @Singular Set<MainIngredient> mainIngredients,
        @Singular Set<SubIngredient> subIngredients) {
        setPost(post);
        this.steps = steps;
        this.mainIngredients = mainIngredients;
        this.subIngredients = subIngredients;
    }

    public void setPost(Post post) {
        if (this.post != post) {
            if (this.post != null)
                this.post.setRecipe(null);
            this.post = post;
            if (post != null)
                post.setRecipe(this);
        }
    }

    public void addStep(Step step) {
        steps.add(step);
        if (!this.equals(step.getRecipe())) {
            step.setRecipe(this);
        }
    }

    public void removeStep(Step step) {
        if (!steps.contains(step))
            throw new StepNotFoundException();

        steps.remove(step);
        if (this.equals(step.getRecipe())) {
            step.setRecipe(null);
        }
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
