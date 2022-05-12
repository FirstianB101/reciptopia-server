package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

	PageImpl<Post> search(PostSearchCondition postSearchCondition, Pageable pageable);
}
