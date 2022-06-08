package kr.reciptopia.reciptopiaserver.business.service.searchcondition;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;

public record PostLikeTagSearchCondition(Long id, List<Long> ids, Long ownerId,
                                         List<Long> ownerIds) {

    @Builder
    public PostLikeTagSearchCondition(Long id, List<Long> ids,
        Long ownerId, List<Long> ownerIds) {
        this.id = id;
        this.ids = ids == null ? new ArrayList<>() : ids;
        this.ownerId = ownerId;
        this.ownerIds = ownerIds == null ? new ArrayList<>() : ownerIds;
    }
}
