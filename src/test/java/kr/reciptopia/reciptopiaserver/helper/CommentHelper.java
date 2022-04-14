package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;

public class CommentHelper {

    private static final Long ARBITRARY_OWNER_ID = 0L;
    private static final Long ARBITRARY_POST_ID = 0L;
    private static final String ARBITRARY_CONTENT = "테스트 댓글 내용";
    private static final String ARBITRARY_NEW_CONTENT = "새 댓글 내용";

    public static Comment aComment() {
        Comment comment = Comment.builder()
            .content(ARBITRARY_CONTENT)
            .build();
        comment.setId(0L);
        comment.setOwner(anAccount());
        comment.setPost(aPost());
        return comment;
    }

    public static Create aCommentCreateDto() {
        return Create.builder()
            .ownerId(ARBITRARY_OWNER_ID)
            .postId(ARBITRARY_POST_ID)
            .content(ARBITRARY_CONTENT)
            .build();
    }

    public static Update aCommentUpdateDto() {
        return Update.builder()
            .content(ARBITRARY_NEW_CONTENT)
            .build();
    }

}
