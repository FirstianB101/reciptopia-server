package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;

public class CommentHelper {

    private static final String ARBITRARY_CONTENT = "테스트 댓글 내용";

    public static Comment aComment() {
        Comment comment = Comment.builder()
            .content(ARBITRARY_CONTENT)
            .build();
        comment.setId(0L);
        comment.setOwner(anAccount());
        comment.setPost(aPost());
        return comment;
    }

    public static Update aCommentUpdateDto() {
        return Update.builder()
            .content(ARBITRARY_CONTENT)
            .build();
    }

}
