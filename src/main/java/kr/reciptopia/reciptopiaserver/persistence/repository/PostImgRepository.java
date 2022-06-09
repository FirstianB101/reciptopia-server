package kr.reciptopia.reciptopiaserver.persistence.repository;

import java.util.List;
import kr.reciptopia.reciptopiaserver.domain.model.PostImg;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImgRepository extends BaseRepository<PostImg, Long> {

	List<PostImg> findAllByPostId(Long postId);
}
