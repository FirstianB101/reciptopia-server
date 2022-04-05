package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PostAuthorizer extends AbstractAuthorizer {

    public PostAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public void requirePostOwner(Authentication authentication, Post requestedPost) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requestedPost.getOwner()) {
            throw errorHelper.forbidden("Not the owner of the post");
        }
    }

}
