package kr.reciptopia.reciptopiaserver.helper;


import static kr.reciptopia.reciptopiaserver.helper.StepHelper.aStep;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;

public class StepImgHelper {

	private static final Long ARBITRARY_ID = 0L;
	private static final Long ARBITRARY_STEP_ID = 1L;
	private static final String ARBITRARY_UPLOAD_FILE_NAME = "testStepImg.jpeg";
	private static final String ARBITRARY_STORE_FILE_NAME
		= "1dee2946-0aba-4b77-b3b2-bb24df7b61a2.jpeg";

	public static StepImg aStepImg() {
		return StepImg.builder()
			.uploadFileName(ARBITRARY_UPLOAD_FILE_NAME)
			.storeFileName(ARBITRARY_STORE_FILE_NAME)
			.step(aStep().withId(ARBITRARY_STEP_ID))
			.build()
			.withId(ARBITRARY_ID);
	}

}