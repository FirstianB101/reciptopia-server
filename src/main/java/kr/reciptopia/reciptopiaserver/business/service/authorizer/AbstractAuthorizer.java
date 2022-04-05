package kr.reciptopia.reciptopiaserver.business.service.authorizer;


import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

@AllArgsConstructor
public class AbstractAuthorizer {

    protected final AuthenticationInspector authInspector;
    protected final ServiceErrorHelper errorHelper;

    public void requireByOneself(Authentication authentication, Account requester) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requester) {
            throw errorHelper.forbidden("Not requested oneself");
        }
    }
}
