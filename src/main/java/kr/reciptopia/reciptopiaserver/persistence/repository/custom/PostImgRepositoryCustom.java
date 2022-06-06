package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.PostImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PostImgRepositoryCustom {

	PageImpl<PostImg> search(
		PostImgSearchCondition searchCondition, Pageable pageable);

}
