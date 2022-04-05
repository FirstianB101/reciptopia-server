package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Recipe;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class RecipeAuthorizer extends PostAuthorizer {

    public RecipeAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public void requireRecipeOwner(Authentication authentication, Recipe requestedRecipe) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requestedRecipe.getPost().getOwner()) {
            throw errorHelper.forbidden("Not the owner of the recipe");
        }
    }

}
