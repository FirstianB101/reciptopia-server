package kr.reciptopia.reciptopiaserver.business.service;

import kr.reciptopia.reciptopiaserver.business.service.authorizer.CommentAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.CommentSearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.CommentRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentRepositoryImpl commentRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final CommentAuthorizer commentAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Comment comment = dto.asEntity();

        Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
        commentAuthorizer.requireByOneself(authentication, owner);
        comment.setOwner(owner);

        Post post = repoHelper.findPostOrThrow(dto.postId());
        comment.setPost(post);

        return Result.of(commentRepository.save(comment));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findCommentOrThrow(id));
    }

    public Bulk.Result search(CommentSearchCondition commentSearchCondition, Pageable pageable) {
        PageImpl<Comment> pageImpl = commentRepositoryImpl.search(commentSearchCondition, pageable);
        return Bulk.Result.of(pageImpl);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Comment comment = repoHelper.findCommentOrThrow(id);
        commentAuthorizer.requireCommentOwner(authentication, comment);

        if (dto.content() != null) {
            comment.setContent(dto.content());
        }

        return Result.of(commentRepository.save(comment));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Comment comment = repoHelper.findCommentOrThrow(id);
        commentAuthorizer.requireCommentOwner(authentication, comment);

        commentRepository.delete(comment);
    }
}
