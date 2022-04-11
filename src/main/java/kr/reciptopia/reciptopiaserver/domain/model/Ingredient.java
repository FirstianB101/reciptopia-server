package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@ToString
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Ingredient extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ingredient_id")
    protected Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "recipe_id")
    @NotNull
    protected Recipe recipe;

    @NotEmpty
    protected String name;

    @NotEmpty
    protected String detail;

    protected Ingredient(Recipe recipe, String name, String detail) {
        this.recipe = recipe;
        this.name = name;
        this.detail = detail;
    }

    public void setRecipe(Recipe recipe) {
        if (this.recipe != recipe) {
            if (this.recipe != null && recipe != null)
                this.recipe.removeIngredient(this);
            this.recipe = recipe;
            if (recipe != null)
                recipe.addIngredient(this);
        }
    }
}
