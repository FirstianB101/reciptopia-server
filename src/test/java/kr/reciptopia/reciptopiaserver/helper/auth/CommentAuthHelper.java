package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentAuthHelper extends AuthHelper {

    public CommentAuthHelper(JwtService jwtService) {
        super(jwtService);
    }

    public String generateToken(Comment comment) {
        return generateToken(comment.getOwner());
    }
}