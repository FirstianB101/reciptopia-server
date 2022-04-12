package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SearchHistoryAuthorizer extends AbstractAuthorizer {

    public SearchHistoryAuthorizer(
        AuthenticationInspector authInspector,
        ServiceErrorHelper errorHelper) {
        super(authInspector, errorHelper);
    }

    public void requireSearchHistoryOwner(Authentication authentication,
        SearchHistory requestedSearchHistory) {
        if (authInspector.isAdmin(authentication))
            return;
        Account account = authInspector.getAccountOrThrow(authentication);

        if (account != requestedSearchHistory.getOwner()) {
            throw errorHelper.forbidden("Not the owner of the searchHistory");
        }
    }

}
