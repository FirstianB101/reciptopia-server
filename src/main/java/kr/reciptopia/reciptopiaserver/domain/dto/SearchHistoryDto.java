package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface SearchHistoryDto {

    @With
    record Create(
        Long ownerId, Set<String> ingredientNames
    ) {

        @Builder
        public Create(
            @NotNull
                Long ownerId,

            @Singular
                Set<String> ingredientNames) {
            this.ownerId = ownerId;
            this.ingredientNames = ingredientNames;
        }
    }

    @With
    record Result(
        Long id, Long ownerId, Set<String> ingredientNames
    ) {

        @Builder
        public Result(
            @NotNull Long id,
            @NotNull Long ownerId,
            @Singular Set<String> ingredientNames) {
            this.id = id;
            this.ownerId = ownerId;
            this.ingredientNames = ingredientNames;
        }

        public static Result of(SearchHistory searchHistory) {
            return Result.builder()
                .id(searchHistory.getId())
                .ownerId(searchHistory.getOwner().getId())
                .ingredientNames(searchHistory.getIngredientNames())
                .build();
        }
    }
}
