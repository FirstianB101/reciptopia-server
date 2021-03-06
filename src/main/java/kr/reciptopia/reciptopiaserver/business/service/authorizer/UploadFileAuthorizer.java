package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import kr.reciptopia.reciptopiaserver.domain.model.UploadFile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UploadFileAuthorizer extends AbstractAuthorizer {

	public UploadFileAuthorizer(
		AuthenticationInspector authInspector,
		ServiceErrorHelper errorHelper) {
		super(authInspector, errorHelper);
	}

	public <T extends UploadFile> void requireUploadFileOwner(Authentication authentication,
		T requestedUploadFile) {
		if (authInspector.isAdmin(authentication))
			return;

		Account account = authInspector.getAccountOrThrow(authentication);
		if (requestedUploadFile instanceof AccountProfileImg) {
			if (account != ((AccountProfileImg) requestedUploadFile).getOwner())
				throw errorHelper.forbidden("Not the owner of the AccountProfileImg");
		}
		if (requestedUploadFile instanceof PostImg) {
			if (account != ((PostImg) requestedUploadFile).getPost().getOwner())
				throw errorHelper.forbidden("Not the owner of the PostImg");
		}
		if (requestedUploadFile instanceof StepImg) {
			if (account != ((StepImg) requestedUploadFile).getStep()
				.getRecipe().getPost().getOwner())
				throw errorHelper.forbidden("Not the owner of the StepImg");
		}
	}

}
