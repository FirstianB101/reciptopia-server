package kr.reciptopia.reciptopiaserver.business.service;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.ReplyAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

    private final ReplyRepository ReplyRepository;
    private final RepositoryHelper repoHelper;
    private final ReplyAuthorizer replyAuthorizer;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
        Comment comment = repoHelper.findCommentOrThrow(dto.commentId());
        replyAuthorizer.requireByOneself(authentication, owner);

        Reply reply = dto.asEntity(it -> it
            .withOwner(owner)
            .withComment(comment)
        );

        return Result.of(ReplyRepository.save(reply));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findReplyOrThrow(id));
    }

    public List<Result> search(Specification<Reply> spec, Pageable pageable) {
        Page<Reply> entities = ReplyRepository.findAll(spec, pageable);
        return Result.of(entities);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Reply reply = repoHelper.findReplyOrThrow(id);
        replyAuthorizer.requireReplyOwner(authentication, reply);

        if (dto.content() != null) {
            reply.setContent(dto.content());
        }

        return Result.of(ReplyRepository.save(reply));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Reply reply = repoHelper.findReplyOrThrow(id);
        replyAuthorizer.requireReplyOwner(authentication, reply);

        ReplyRepository.delete(reply);
    }
}