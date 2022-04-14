package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.domain.model.Favorite;
import org.springframework.stereotype.Component;
import kr.reciptopia.reciptopiaserver.business.service.JwtService;

@Component
public class FavoriteAuthHelper extends AuthHelper {

    public FavoriteAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(Favorite favorite) {
        return generateToken(favorite.getOwner());
    }
}