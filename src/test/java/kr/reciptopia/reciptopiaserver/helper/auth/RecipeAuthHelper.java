package kr.reciptopia.reciptopiaserver.helper.auth;


import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import org.springframework.stereotype.Component;

@Component
public class RecipeAuthHelper extends PostAuthHelper {

    public RecipeAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(Recipe recipe) {
        return generateToken(recipe.getPost());
    }
}