package kr.reciptopia.reciptopiaserver.helper.auth;


import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthHelper {

    protected final JwtService jwtService;

    public String generateToken(Account account) {
        return jwtService.signJwt(account);
    }
}
