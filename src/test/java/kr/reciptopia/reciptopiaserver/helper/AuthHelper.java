package kr.reciptopia.reciptopiaserver.helper;


import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import org.springframework.stereotype.Component;

@Component
public record AuthHelper(JwtService jwtService) {

    public String generateToken(Account account) {
        return jwtService.signJwt(account);
    }
}
