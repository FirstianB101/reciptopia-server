package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.PostLikeTagDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.helper.InitializationHelper.noInit;
import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;
import static kr.reciptopia.reciptopiaserver.helper.fieldfilter.FilterHelper.getNonNull;

import java.util.function.Function;
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

    public static Create aPostLikeTagCreateDto(
        Function<? super Create, ? extends Create> initialize) {
        Create createDto = Create.builder()
            .build();

        createDto = initialize.apply(createDto);
        return Create.builder()
            .ownerId(getNonNull(createDto.ownerId(), ARBITRARY_OWNER_ID))
            .postId(getNonNull(createDto.postId(), ARBITRARY_POST_ID))
            .build();
    }

    public static Create aPostLikeTagCreateDto() {
        return aPostLikeTagCreateDto(noInit());
    }

    public static Result aPostLikeTagResultDto() {
        return Result.builder()
            .ownerId(ARBITRARY_OWNER_ID)
            .postId(ARBITRARY_POST_ID)
            .build();
    }
}
