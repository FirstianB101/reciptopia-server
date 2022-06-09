package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

public record PostLikeTagSearchCondition(List<Long> ids, List<Long> ownerIds) {

    @Builder
    public PostLikeTagSearchCondition(List<Long> ids, List<Long> ownerIds) {
        this.ids = ids == null ? new ArrayList<>() : ids;
        this.ownerIds = ownerIds == null ? new ArrayList<>() : ownerIds;
    }
}
