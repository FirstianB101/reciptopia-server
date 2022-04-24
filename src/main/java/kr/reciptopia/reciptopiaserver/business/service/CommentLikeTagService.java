package kr.reciptopia.reciptopiaserver.business.service;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.LikeTagAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentLikeTagDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentLikeTagDto.Result;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.CommentLikeTag;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentLikeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeTagService {

	private final CommentLikeTagRepository commentLikeTagRepository;
	private final RepositoryHelper repoHelper;
	private final LikeTagAuthorizer likeTagAuthorizer;

	@Transactional
	public Result create(Create dto, Authentication authentication) {
		Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
		Comment comment = repoHelper.findCommentOrThrow(dto.commentId());
		likeTagAuthorizer.requireByOneself(authentication, owner);

		CommentLikeTag commentLikeTag = dto.asEntity(it -> it
			.withOwner(owner)
			.withComment(comment)
		);

		return Result.of(commentLikeTagRepository.save(commentLikeTag));
	}

	public Result read(Long id) {
		return Result.of(repoHelper.findCommentLikeTagOrThrow(id));
	}

	public List<Result> search(Pageable pageable) {
		Page<CommentLikeTag> entities = commentLikeTagRepository.findAll(pageable);
		return Result.of(entities);
	}

	@Transactional
	public void delete(Long id, Authentication authentication) {
		CommentLikeTag commentLikeTag = repoHelper.findCommentLikeTagOrThrow(id);
		likeTagAuthorizer.requireLikeTagOwner(authentication, commentLikeTag);

		commentLikeTagRepository.delete(commentLikeTag);
	}
}
