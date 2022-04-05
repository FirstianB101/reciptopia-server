package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Ingredient;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class IngredientAuthorizer extends RecipeAuthorizer {

    public IngredientAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public <T extends Ingredient> void requireIngredientOwner(Authentication authentication,
        T requestedIngredient) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requestedIngredient.getRecipe().getPost().getOwner()) {
            throw errorHelper.forbidden("Not the owner of the ingredient");
        }
    }

}
