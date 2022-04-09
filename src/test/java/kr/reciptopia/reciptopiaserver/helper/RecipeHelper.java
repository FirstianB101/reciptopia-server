package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.RecipeDto.Result;
import static kr.reciptopia.reciptopiaserver.helper.PostHelper.aPost;

import kr.reciptopia.reciptopiaserver.domain.model.Recipe;

public class RecipeHelper {

    private static final Long ARBITRARY_ID = 0L;
    private static final Long ARBITRARY_POST_ID = 0L;

    public static Recipe aRecipe() {
        return Recipe.builder()
            .post(aPost().withId(ARBITRARY_POST_ID))
            .build()
            .withId(ARBITRARY_ID);
    }

    public static Create aRecipeCreateDto() {
        return Create.builder()
            .postId(ARBITRARY_POST_ID)
            .build();
    }

    public static Result aRecipeResultDto() {
        return Result.builder()
            .id(ARBITRARY_ID)
            .postId(ARBITRARY_POST_ID)
            .build();
    }

}
