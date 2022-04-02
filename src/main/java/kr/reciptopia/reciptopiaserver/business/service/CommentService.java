package kr.reciptopia.reciptopiaserver.business.service;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.AbstractAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.CommentDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import kr.reciptopia.reciptopiaserver.persistence.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final RepositoryHelper repoHelper;
    private final AbstractAuthorizer authorizer;

    @Autowired
    public CommentService(
        CommentRepository commentRepository,
        RepositoryHelper repoHelper,
        AbstractAuthorizer authorizer) {
        this.commentRepository = commentRepository;
        this.repoHelper = repoHelper;
        this.authorizer = authorizer;
    }

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Comment comment = dto.asEntity();

        Account dtoOwner = repoHelper.findAccountOrThrow(dto.ownerId());
        authorizer.requireByOneself(authentication, dtoOwner);
        comment.setOwner(dtoOwner);

        Post dtoPost = repoHelper.findPostOrThrow(dto.postId());
        comment.setPost(dtoPost);

        return Result.of(commentRepository.save(comment));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findCommentOrThrow(id));
    }

    public List<Result> search(Specification<Comment> spec, Pageable pageable) {
        Page<Comment> entities = commentRepository.findAll(spec, pageable);
        return Result.of(entities);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Comment entity = repoHelper.findCommentOrThrow(id);
        authorizer.requireByOneself(authentication, entity.getOwner());

        if (dto.content() != null) {
            entity.setContent(dto.content());
        }

        return Result.of(commentRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Comment comment = repoHelper.findCommentOrThrow(id);
        authorizer.requireByOneself(authentication, comment.getOwner());
        comment.removeAllCollections();

        commentRepository.delete(comment);
    }
}
