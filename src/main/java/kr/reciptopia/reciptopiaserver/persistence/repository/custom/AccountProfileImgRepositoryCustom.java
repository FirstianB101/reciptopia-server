package kr.reciptopia.reciptopiaserver.persistence.repository.custom;

import kr.reciptopia.reciptopiaserver.business.service.searchcondition.AccountProfileImgSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface AccountProfileImgRepositoryCustom {

	PageImpl<AccountProfileImg> search(
		AccountProfileImgSearchCondition searchCondition, Pageable pageable);
}
