package kr.reciptopia.reciptopiaserver.business.service;

import kr.reciptopia.reciptopiaserver.business.service.authorizer.ReplyAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.business.service.searchcondition.ReplySearchCondition;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Bulk;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Create;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Result;
import kr.reciptopia.reciptopiaserver.domain.dto.ReplyDto.Update;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import kr.reciptopia.reciptopiaserver.persistence.repository.ReplyRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.ReplyRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

    private final ReplyRepository ReplyRepository;
    private final ReplyRepositoryImpl replyRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final ReplyAuthorizer replyAuthorizer;
    private final ServiceErrorHelper errorHelper;

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

    public Bulk.Result search(ReplySearchCondition replySearchCondition, Pageable pageable) {
        PageImpl<Reply> pageImpl = replyRepositoryImpl.search(replySearchCondition, pageable);
        return Bulk.Result.of(pageImpl);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Reply reply = repoHelper.findReplyOrThrow(id);
        replyAuthorizer.requireReplyOwner(authentication, reply);

        if (dto.content() != null) {
            throwExceptionWhenBlankContent(dto.content());
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

    public void throwExceptionWhenBlankContent(String content) {
        if (content.isBlank()) {
            throw errorHelper.badRequest(
                "Content must not be null and must contain at least one non-whitespace character");
        }
    }
}