package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ReplyAuthorizer extends AbstractAuthorizer {

    public ReplyAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public void requireReplyOwner(Authentication authentication, Reply requestedReply) {
        if (authInspector.isAdmin(authentication))
            return;

        Account account = authInspector.getAccountOrThrow(authentication);
        if (account != requestedReply.getOwner()) {
            throw errorHelper.forbidden("Not the owner of the reply");
        }
    }

}
