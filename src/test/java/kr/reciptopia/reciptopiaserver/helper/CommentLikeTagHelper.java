package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.CommentHelper.aComment;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentLikeTagDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;

public class CommentLikeTagHelper {

	private static final Long ARBITRARY_ID = 0L;
	private static final Long ARBITRARY_OWNER_ID = 0L;
	private static final Long ARBITRARY_COMMENT_ID = 0L;

	public static CommentLikeTag aCommentLikeTag() {
		return CommentLikeTag.builder()
			.owner(anAccount().withId(ARBITRARY_OWNER_ID))
			.comment(aComment().withId(ARBITRARY_COMMENT_ID))
			.build()
			.withId(ARBITRARY_ID);
	}

	public static Create aCommentLikeTagCreateDto() {
		return Create.builder()
			.ownerId(ARBITRARY_OWNER_ID)
			.commentId(ARBITRARY_COMMENT_ID)
			.build();
	}

	public static Result aCommentLikeTagResultDto() {
		return Result.builder()
			.ownerId(ARBITRARY_OWNER_ID)
			.commentId(ARBITRARY_COMMENT_ID)
			.build();
	}

}
