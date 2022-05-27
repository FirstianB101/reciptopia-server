package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;

public class AccountProfileImgHelper {

	private static final Long ARBITRARY_ID = 0L;
	private static final Long ARBITRARY_OWNER_ID = 1L;
	private static final String ARBITRARY_UPLOAD_FILE_NAME = "testProfileImg.png";
	private static final String ARBITRARY_STORE_FILE_NAME
		= "1dee2946-0aba-4b77-b3b2-bb24df7b61a2.png";

	public static AccountProfileImg anAccountProfileImg() {
		return AccountProfileImg.builder()
			.uploadFileName(ARBITRARY_UPLOAD_FILE_NAME)
			.storeFileName(ARBITRARY_STORE_FILE_NAME)
			.owner(anAccount().withId(ARBITRARY_OWNER_ID))
			.build()
			.withId(ARBITRARY_ID);
	}

}
