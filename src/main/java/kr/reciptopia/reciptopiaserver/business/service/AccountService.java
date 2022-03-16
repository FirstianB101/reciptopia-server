package kr.reciptopia.reciptopiaserver.business.service;


import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.CheckDuplicationResult;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Result;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Update;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.AbstractAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final RepositoryHelper repoHelper;
    private final ServiceErrorHelper errorHelper;
    private final AbstractAuthorizer authorizer;

    @Autowired
    public AccountService(PasswordEncoder passwordEncoder,
        AccountRepository accountRepository,
        RepositoryHelper repoHelper,
        ServiceErrorHelper errorHelper,
        AbstractAuthorizer authorizer) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.repoHelper = repoHelper;
        this.errorHelper = errorHelper;
        this.authorizer = authorizer;
    }

    @Transactional
    public Result create(Create dto) {
        throwExceptionWhenDuplicateEmail(dto.getEmail());
        Account account = dto.asEntity(it -> it
            .withRole(UserRole.USER)
            .withPassword(passwordEncoder::encode)
        );

        return Result.of(accountRepository.save(account));
    }

    public Result read(Long id) {
        return Result.of(repoHelper.findAccountOrThrow(id));
    }

    public List<Result> search(Specification<Account> spec, Pageable pageable) {
        Page<Account> entities = accountRepository.findAll(spec, pageable);
        return Result.of(entities);
    }

    @Transactional
    public Result update(Long id, Update dto, Authentication authentication) {
        Account entity = repoHelper.findAccountOrThrow(id);
        authorizer.requireByOneself(authentication, entity);

        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            entity.setPassword(passwordEncoder::encode, dto.getPassword());
        }
        if (dto.getNickname() != null) {
            entity.setNickname(dto.getNickname());
        }
        if (dto.getProfilePictureUrl() != null) {
            entity.setProfilePictureUrl(entity.getProfilePictureUrl());
        }

        return Result.of(accountRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        Account account = repoHelper.findAccountOrThrow(id);
        authorizer.requireByOneself(authentication, account);
        account.removeAllCollections();

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
