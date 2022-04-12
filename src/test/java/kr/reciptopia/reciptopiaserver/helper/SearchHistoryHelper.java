package kr.reciptopia.reciptopiaserver.helper;

import static kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto.Result;
import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;

import java.util.Set;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;

public class SearchHistoryHelper {

    private static final Long ARBITRARY_OWNER_ID = 0L;
    private static final Long ARBITRARY_SEARCH_HISTORY_ID = 0L;
    private static final Set<String> ARBITRARY_INGRESIENT_NAMES = Set.of("된장", "감자", "두부");

    public static SearchHistory aSearchHistory() {
        return SearchHistory.builder()
            .build()
            .withOwner(anAccount().withId(ARBITRARY_OWNER_ID))
            .withIngredientNames(ARBITRARY_INGRESIENT_NAMES)
            .withId(ARBITRARY_SEARCH_HISTORY_ID);
    }

    public static Create aSearchHistoryCreateDto() {
        return Create.builder()
            .ownerId(ARBITRARY_OWNER_ID)
            .ingredientNames(ARBITRARY_INGRESIENT_NAMES)
            .build();
    }

    public static Result aSearchHistoryResultDto() {
        return Result.builder()
            .id(ARBITRARY_OWNER_ID)
            .ownerId(ARBITRARY_OWNER_ID)
            .ingredientNames(ARBITRARY_INGRESIENT_NAMES)
            .build();
    }

}
