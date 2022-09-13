package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Create;
import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;

import kr.reciptopia.reciptopiaserver.domain.dto.PostDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Post;

public class PostHelper {

    private static final Long ARBITRARY_ID = 0L;
    private static final Long ARBITRARY_OWNER_ID = 4L;
    private static final String ARBITRARY_TITLE = "가문어 오징어 튀김 만들기";
    private static final String ARBITRARY_CONTENT = "가문어 오징어 다리를 잘라서 튀긴다.";

    // 임의의 지정된 필드 값으로 채워진 Post 반환
    public static Post aPost() {
        return Post.builder()
            .owner(anAccount())
            .title(ARBITRARY_TITLE)
            .content(ARBITRARY_CONTENT)
            .build()
            .withId(ARBITRARY_ID);
    }

    public static Create aPostCreateDto() {
        return Create.builder()
            .ownerId(ARBITRARY_OWNER_ID)
            .title(ARBITRARY_TITLE)
            .content(ARBITRARY_CONTENT)
            .build();
    }

    public static Update aPostUpdateDto() {
        return Update.builder()
            .title(ARBITRARY_TITLE)
            .content(ARBITRARY_CONTENT)
            .build();
    }

}
