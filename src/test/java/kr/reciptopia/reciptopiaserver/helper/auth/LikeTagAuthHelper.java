package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.LikeTag;
import org.springframework.stereotype.Component;

@Component
public class LikeTagAuthHelper extends AuthHelper {

	public LikeTagAuthHelper(JwtService jwtService) {
		super(jwtService);
	}

	public String generateToken(LikeTag likeTag) {
		return generateToken(likeTag.getOwner());
	}
}
