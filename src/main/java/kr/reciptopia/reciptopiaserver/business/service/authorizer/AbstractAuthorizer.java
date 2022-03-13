package kr.reciptopia.reciptopiaserver.business.service.authorizer;


import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import org.springframework.security.core.Authentication;

public record AbstractAuthorizer(
    AuthenticationInspector authInspector,
    ServiceErrorHelper errorHelper) {

    public void requireByOneself(Authentication authentication, Account requester) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requester) {
            throw errorHelper.forbidden("Not requested oneself");
        }
    }
}
