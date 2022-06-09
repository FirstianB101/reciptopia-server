package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostLikeTagSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PostLikeTagRepositoryCustom {

    PageImpl<PostLikeTag> search(PostLikeTagSearchCondition postLikeTagSearchCondition,
        Pageable pageable);
}

