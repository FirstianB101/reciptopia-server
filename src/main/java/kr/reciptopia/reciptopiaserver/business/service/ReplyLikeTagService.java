package kr.reciptopia.reciptopiaserver.business.service;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.LikeTagAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyLikeTagDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.domain.model.ReplyLikeTag;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyLikeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyLikeTagService {

	private final ReplyLikeTagRepository replyLikeTagRepository;
	private final RepositoryHelper repoHelper;
	private final LikeTagAuthorizer likeTagAuthorizer;

	@Transactional
	public Result create(Create dto, Authentication authentication) {
		Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
		Reply reply = repoHelper.findReplyOrThrow(dto.replyId());
		likeTagAuthorizer.requireByOneself(authentication, owner);

		ReplyLikeTag replyLikeTag = dto.asEntity(it -> it
			.withOwner(owner)
			.withReply(reply)
		);

		return Result.of(replyLikeTagRepository.save(replyLikeTag));
	}

	public Result read(Long id) {
		return Result.of(repoHelper.findReplyLikeTagOrThrow(id));
	}

	public List<Result> search(Pageable pageable) {
		Page<ReplyLikeTag> entities = replyLikeTagRepository.findAll(pageable);
		return Result.of(entities);
	}

	@Transactional
	public void delete(Long id, Authentication authentication) {
		ReplyLikeTag replyLikeTag = repoHelper.findReplyLikeTagOrThrow(id);
		likeTagAuthorizer.requireLikeTagOwner(authentication, replyLikeTag);

		replyLikeTagRepository.delete(replyLikeTag);
	}
}
