package kr.reciptopia.reciptopiaserver.business.service;


import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Bulk;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.CheckDuplicationResult;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Update;

import kr.reciptopia.reciptopiaserver.business.service.authorizer.AccountAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import kr.reciptopia.reciptopiaserver.persistence.repository.implementaion.AccountRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AccountRepositoryImpl accountRepositoryImpl;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;
    private final AccountAuthorizer accountAuthorizer;

    @Transactional
    public Result create(Create dto) {
        throwExceptionWhenDuplicateEmail(dto.email());
        Account account = dto.asEntity(it -> it
            .withRole(UserRole.USER)
            .withPassword(passwordEncoder::encode)
        );

        return Result.of(accountRepository.save(account));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findAccountOrThrow(id));
    }

    public Bulk.Result search(Pageable pageable) {
        return accountRepositoryImpl.search(pageable);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Account entity = repoHelper.findAccountOrThrow(id);
        accountAuthorizer.requireByOneself(authentication, entity);

        if (dto.email() != null) {
            entity.setEmail(dto.email());
        }
        if (dto.password() != null) {
            entity.setPassword(passwordEncoder::encode, dto.password());
        }
        if (dto.nickname() != null) {
            entity.setNickname(dto.nickname());
        }
        if (dto.profilePictureUrl() != null) {
            entity.setProfilePictureUrl(dto.profilePictureUrl());
        }

        return Result.of(accountRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Account account = repoHelper.findAccountOrThrow(id);
        accountAuthorizer.requireByOneself(authentication, account);

        accountRepository.delete(account);
    }

    public void throwExceptionWhenDuplicateEmail(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw errorHelper.badRequest("Already enrolled account");
        }
    }

    public CheckDuplicationResult checkDuplicateEmail(String email) {
        boolean result = accountRepository.existsByEmail(email);
        return CheckDuplicationResult.builder()
            .exists(result)
            .build();
    }
}
