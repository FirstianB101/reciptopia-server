package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import org.springframework.stereotype.Component;

@Component
public class SearchHistoryAuthHelper extends AuthHelper {

    public SearchHistoryAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(SearchHistory searchHistory) {
        return generateToken(searchHistory.getOwner());
    }
}