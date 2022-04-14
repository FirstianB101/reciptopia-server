package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.LikeTag;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class LikeTagAuthorizer extends AbstractAuthorizer {

    public LikeTagAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public <T extends LikeTag> void requireLikeTagOwner(Authentication authentication,
        T requestedLikeTag) {
        if (authInspector.isAdmin(authentication))
            return;

        Account account = authInspector.getAccountOrThrow(authentication);
        if (account != requestedLikeTag.getOwner()) {
            if (requestedLikeTag instanceof PostLikeTag)
                throw errorHelper.forbidden("Not the owner of the PostLikeTag");
        }
    }

}