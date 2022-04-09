package kr.reciptopia.reciptopiaserver.helper;

import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;

public class ReplyHelper {

    private static final String ARBITRARY_CONTENT = "테스트 답글 내용";

    public static Reply aReply() {
        return Reply.builder()
            .build()
            .withContent(ARBITRARY_CONTENT)
            .withId(0L);
    }

    public static Create aReplyCreateDto() {
        return Create.builder()
            .build();
    }

    public static Update aReplyUpdateDto() {
        return Update.builder()
            .build();
    }

    public static Result aReplyResultDto() {
        return Result.builder()
            .build();
    }

}
