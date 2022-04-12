package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;

public class PostLikeTagHelper {

	private static final Long ARBITRARY_ID = 0L;
	private static final Long ARBITRARY_OWNER_ID = 0L;
	private static final Long ARBITRARY_POST_ID = 0L;

	public static PostLikeTag aPostLikeTag() {
		return PostLikeTag.builder()
			.owner(anAccount().withId(ARBITRARY_OWNER_ID))
			.post(aPost().withId(ARBITRARY_POST_ID))
			.build()
			.withId(ARBITRARY_ID);
	}

	public static Create aPostLikeTagCreateDto() {
		return Create.builder()
			.ownerId(ARBITRARY_OWNER_ID)
			.postId(ARBITRARY_POST_ID)
			.build();
	}

	public static Result aPostLikeTagResultDto() {
		return Result.builder()
			.ownerId(ARBITRARY_OWNER_ID)
			.postId(ARBITRARY_POST_ID)
			.build();
	}

}
