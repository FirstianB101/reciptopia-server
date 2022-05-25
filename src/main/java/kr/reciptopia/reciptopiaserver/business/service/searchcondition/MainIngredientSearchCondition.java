package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

public record MainIngredientSearchCondition(Long recipeId, Long postId, List<Long> ids,
                                            List<Long> recipeIds,
                                            List<Long> postIds) {

    @Builder
    public MainIngredientSearchCondition(Long recipeId, Long postId, List<Long> ids,
        List<Long> recipeIds,
        List<Long> postIds) {
        this.recipeId = recipeId;
        this.postId = postId;
        this.ids = ids == null ? new ArrayList<>() : ids;
        this.recipeIds = recipeIds == null ? new ArrayList<>() : recipeIds;
        this.postIds = postIds == null ? new ArrayList<>() : postIds;
    }
}
