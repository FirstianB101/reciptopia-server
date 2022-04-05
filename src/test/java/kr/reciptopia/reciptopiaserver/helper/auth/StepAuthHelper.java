package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import org.springframework.stereotype.Component;

@Component
public class StepAuthHelper extends RecipeAuthHelper {

    public StepAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(Step step) {
        return generateToken(step.getRecipe());
    }
}