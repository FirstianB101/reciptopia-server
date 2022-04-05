package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import org.springframework.stereotype.Component;

@Component
public class PostAuthHelper extends AuthHelper {

    public PostAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(Post post) {
        return generateToken(post.getOwner());
    }
}