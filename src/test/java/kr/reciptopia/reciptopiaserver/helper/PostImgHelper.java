package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;

public class PostImgHelper {

	private static final Long ARBITRARY_ID = 0L;
	private static final Long ARBITRARY_POST_ID = 1L;
	private static final String ARBITRARY_UPLOAD_FILE_NAME = "testPostImg.png";
	private static final String ARBITRARY_STORE_FILE_NAME
		= "1dee2946-0aba-4b77-b3b2-bb24df7b61a2.png";

	public static PostImg aPostImg() {
		return PostImg.builder()
			.uploadFileName(ARBITRARY_UPLOAD_FILE_NAME)
			.storeFileName(ARBITRARY_STORE_FILE_NAME)
			.post(aPost().withId(ARBITRARY_POST_ID))
			.build()
			.withId(ARBITRARY_ID);
	}

}
