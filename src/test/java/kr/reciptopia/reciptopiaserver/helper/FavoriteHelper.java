package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto.*;
import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;

import java.util.UUID;
import kr.reciptopia.reciptopiaserver.domain.dto.FavoriteDto;
import kr.reciptopia.reciptopiaserver.domain.model.Favorite;

public class FavoriteHelper {

    private static final Long ARBITRARY_FAVORITE_ID = 0L;
    private static final Long ARBITRARY_OWNER_ID = 0L;
    private static final Long ARBITRARY_POST_ID = 0L;

    public static Favorite aFavorite() {
        return Favorite.builder()
            .owner(anAccount().withId(ARBITRARY_OWNER_ID))
            .post(aPost().withId(ARBITRARY_POST_ID))
            .build()
            .withId(ARBITRARY_FAVORITE_ID);
    }

    public static Create aFavoriteCreateDto() {
        return Create.builder()
            .ownerId(ARBITRARY_OWNER_ID)
            .postId(ARBITRARY_POST_ID)
            .build();
    }


    public static Result aFavoriteResultDto() {
        return Result.builder()
            .id(ARBITRARY_FAVORITE_ID)
            .ownerId(ARBITRARY_OWNER_ID)
            .postId(ARBITRARY_POST_ID)
            .build();
    }

}
