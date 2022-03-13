package kr.reciptopia.reciptopiaserver.business.service.authorizer;

import kr.reciptopia.reciptopiaserver.business.service.helper.ServiceErrorHelper;
import kr.reciptopia.reciptopiaserver.config.security.UserPrincipal;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public record AuthenticationInspector(
    AccountRepository accountRepository,
    ServiceErrorHelper errorHelper) {

    public Account getAccountOrThrow(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw errorHelper.unauthorized("Not authenticated");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return accountRepository.findById(principal.getId())
            .orElseThrow(() -> errorHelper.forbidden("Removed account"));
    }

    public boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw errorHelper.unauthorized("Not authenticated");
        }
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
    }

}
