package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import org.springframework.stereotype.Component;

@Component
public class ReplyAuthHelper extends AuthHelper {

    public ReplyAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(Reply reply) {
        return generateToken(reply.getOwner());
    }
}