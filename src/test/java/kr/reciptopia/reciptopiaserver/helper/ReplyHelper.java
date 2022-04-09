package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.CommentHelper.aComment;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;

public class ReplyHelper {

    private static final Long ARBITRARY_OWNER_ID = 1L;
    private static final Long ARBITRARY_COMMENT_ID = 10L;
    private static final String ARBITRARY_CONTENT = "테스트 답글 내용";
    private static final String NEW_ARBITRARY_CONTENT = "새로운 답글 내용";

    public static Reply aReply() {
        return Reply.builder()
            .owner(anAccount().withId(ARBITRARY_OWNER_ID))
            .comment(aComment().withId(ARBITRARY_COMMENT_ID))
            .content(ARBITRARY_CONTENT)
            .build()
            .withId(0L);
    }

    public static Create aReplyCreateDto() {
        return Create.builder()
            .ownerId(ARBITRARY_OWNER_ID)
            .commentId(ARBITRARY_COMMENT_ID)
            .content(ARBITRARY_CONTENT)
            .build();
    }

    public static Update aReplyUpdateDto() {
        return Update.builder()
            .content(NEW_ARBITRARY_CONTENT)
            .build();
    }

    public static Result aReplyResultDto() {
        return Result.builder()
            .ownerId(ARBITRARY_OWNER_ID)
            .commentId(ARBITRARY_COMMENT_ID)
            .content(ARBITRARY_CONTENT)
            .build();
    }

}
