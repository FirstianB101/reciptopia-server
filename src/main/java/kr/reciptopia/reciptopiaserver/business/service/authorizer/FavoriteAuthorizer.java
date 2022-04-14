package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class FavoriteAuthorizer extends AbstractAuthorizer {

    public FavoriteAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public void requireFavoriteOwner(Authentication authentication, Favorite requestedFavorite) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requestedFavorite.getOwner()) {
            throw errorHelper.forbidden("Not the owner of the favorite");
        }
    }

}
