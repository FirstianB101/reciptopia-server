package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.AuthDto.GenerateToken;
import static kr.reciptopia.reciptopiaserver.domain.dto.AuthDto.GenerateTokenResult;
import static kr.reciptopia.reciptopiaserver.domain.dto.AuthDto.MeResult;

import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.config.security.UserPrincipal;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountDto;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public record AuthTokenService(
    JwtService jwtService,
    PasswordEncoder passwordEncoder,
    AccountRepository accountRepository,
    RepositoryHelper repositoryHelper,
    ServiceErrorHelper errorHelper) {

    public GenerateTokenResult generateToken(GenerateToken dto) {
        Account account = accountRepository.findByEmail(dto.email())
            .orElseThrow(() -> errorHelper.unauthorized("Username not found"));

        if (!passwordEncoder.matches(dto.password(), account.getPassword())) {
            throw errorHelper.unauthorized("Wrong password");
        }

        String token = jwtService.signJwt(account);

        return GenerateTokenResult.builder()
            .token(token)
            .account(AccountDto.Result.of(account))
            .build();
    }

    public MeResult getMe(Authentication authentication) {
        if (authentication == null)
            throw errorHelper.unauthorized("Not authenticated");

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Account account = accountRepository.findById(principal.id())
            .orElseThrow(() -> errorHelper.unauthorized("Removed account"));

        return MeResult.builder()
            .account(getAccountDtoIfExists(account.getId()))
            .build();
    }

    private AccountDto.Result getAccountDtoIfExists(Long accountId) {
        return AccountDto.Result.of(repositoryHelper.findAccountOrThrow(accountId));
    }

}
