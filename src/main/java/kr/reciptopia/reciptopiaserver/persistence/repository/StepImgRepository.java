package kr.reciptopia.reciptopiaserver.persistence.repository;

import java.util.Optional;
import kr.reciptopia.reciptopiaserver.domain.model.StepImg;
import org.springframework.stereotype.Repository;

@Repository
public interface StepImgRepository extends BaseRepository<StepImg, Long> {

	Optional<StepImg> findByStepId(Long stepId);
}
