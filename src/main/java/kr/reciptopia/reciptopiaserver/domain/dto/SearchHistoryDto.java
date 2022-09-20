package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.helper.InitializationHelper.noInit;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface SearchHistoryDto {

    interface Bulk {

        @With
        record Result(
            Map<Long, SearchHistoryDto.Result> searchHistories
        ) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, SearchHistoryDto.Result> searchHistories
            ) {
                this.searchHistories = searchHistories;
            }

            public static Result of(Page<SearchHistory> searchHistories) {
                return Result.builder()
                    .searchHistories(
                        (Map<? extends Long, ? extends SearchHistoryDto.Result>) searchHistories.stream()
                            .map(SearchHistoryDto.Result::of)
                            .collect(
                                Collectors.toMap(
                                    SearchHistoryDto.Result::id,
                                    result -> result,
                                    (x, y) -> y,
                                    LinkedHashMap::new)))
                    .build();
            }
        }
    }


    @With
    record Create(
        @NotNull Long ownerId, Set<String> ingredientNames
    ) {

        @Builder
        public Create(
            Long ownerId,
            @Singular
            Set<String> ingredientNames) {
            this.ownerId = ownerId;
            this.ingredientNames = ingredientNames;
        }

        public SearchHistory asEntity(
            Function<? super SearchHistory, ? extends SearchHistory> initialize) {
            return initialize.apply(SearchHistory.builder()
                .ingredientNames(ingredientNames)
                .build());
        }

        public SearchHistory asEntity() {
            return asEntity(noInit());
        }
    }

    @With
    record Result(
        @NotNull Long id,
        @NotNull Long ownerId,
        Set<String> ingredientNames,
        @NotNull LocalDateTime createdDate
    ) {

        @Builder
        public Result(
            Long id,
            Long ownerId,
            @Singular Set<String> ingredientNames,
            LocalDateTime createdDate) {
            this.id = id;
            this.ownerId = ownerId;
            this.ingredientNames = ingredientNames;
            this.createdDate = createdDate;
        }

        public static Result of(SearchHistory searchHistory) {
            return Result.builder()
                .id(searchHistory.getId())
                .ownerId(searchHistory.getOwner().getId())
                .ingredientNames(searchHistory.getIngredientNames())
                .createdDate(searchHistory.getCreatedDate())
                .build();
        }

        public static List<Result> of(Streamable<SearchHistory> searchHistorys) {
            return searchHistorys.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
