package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CommentAuthorizer extends AbstractAuthorizer {

    public CommentAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public void requireCommentOwner(Authentication authentication, Comment requestedComment) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requestedComment.getOwner()) {
            throw errorHelper.forbidden("Not the owner of the comment");
        }
    }

}
