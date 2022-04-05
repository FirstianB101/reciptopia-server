package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import org.springframework.stereotype.Component;

@Component
public class AccountAuthHelper extends AuthHelper {

    public AccountAuthHelper(JwtService jwtService) {
        super(jwtService);
    }
}