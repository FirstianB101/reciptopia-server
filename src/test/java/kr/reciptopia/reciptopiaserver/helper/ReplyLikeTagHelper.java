package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.ReplyHelper.aReply;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyLikeTagDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;

public class ReplyLikeTagHelper {

	private static final Long ARBITRARY_ID = 0L;
	private static final Long ARBITRARY_OWNER_ID = 0L;
	private static final Long ARBITRARY_REPLY_ID = 0L;

	public static ReplyLikeTag aReplyLikeTag() {
		return ReplyLikeTag.builder()
			.owner(anAccount().withId(ARBITRARY_OWNER_ID))
			.reply(aReply().withId(ARBITRARY_REPLY_ID))
			.build()
			.withId(ARBITRARY_ID);
	}

	public static Create aReplyLikeTagCreateDto() {
		return Create.builder()
			.ownerId(ARBITRARY_OWNER_ID)
			.replyId(ARBITRARY_REPLY_ID)
			.build();
	}

	public static Result aReplyLikeTagResultDto() {
		return Result.builder()
			.ownerId(ARBITRARY_OWNER_ID)
			.replyId(ARBITRARY_REPLY_ID)
			.build();
	}

}
