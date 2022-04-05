package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Step;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class StepAuthorizer extends RecipeAuthorizer {

    public StepAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public void requireStepOwner(Authentication authentication, Step requestedStep) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requestedStep.getRecipe().getPost().getOwner()) {
            throw errorHelper.forbidden("Not the owner of the step");
        }
    }

}
