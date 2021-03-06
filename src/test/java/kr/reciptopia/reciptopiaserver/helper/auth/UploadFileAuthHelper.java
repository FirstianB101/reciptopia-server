package kr.reciptopia.reciptopiaserver.helper.auth;

import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import kr.reciptopia.reciptopiaserver.domain.model.UploadFile;
import kr.reciptopia.reciptopiaserver.helper.auth.error.exception.UploadFileInvalidTypeException;
import org.springframework.stereotype.Component;

@Component
public class UploadFileAuthHelper extends AuthHelper {

	public UploadFileAuthHelper(JwtService jwtService) {
		super(jwtService);
	}

	public String generateToken(UploadFile uploadFile) {
		if (uploadFile instanceof AccountProfileImg) {
			return generateToken(((AccountProfileImg) uploadFile).getOwner());
		}
		if (uploadFile instanceof PostImg) {
			return generateToken(((PostImg) uploadFile).getPost().getOwner());
		}
		if (uploadFile instanceof StepImg) {
			return generateToken(
				((StepImg) uploadFile).getStep().getRecipe().getPost().getOwner());
		}

		throw new UploadFileInvalidTypeException();
	}
}