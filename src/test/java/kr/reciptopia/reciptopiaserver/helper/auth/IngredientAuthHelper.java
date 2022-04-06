package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientAuthHelper extends RecipeAuthHelper {

    public IngredientAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(Ingredient ingredient) {
        return generateToken(ingredient.getRecipe());
    }
}