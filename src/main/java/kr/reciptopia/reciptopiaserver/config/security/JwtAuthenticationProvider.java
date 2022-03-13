package kr.reciptopia.reciptopiaserver.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.persistence.repository.AccountRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public record JwtAuthenticationProvider(
    JwtService jwtService,
    AccountRepository accountRepository) implements
    AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        SecurityContext context = SecurityContextHolder.getContext();
        JwtAuthenticationToken auth = (JwtAuthenticationToken) context.getAuthentication();

        String token = auth.getToken();
        UserPrincipal principal;
        try {
            principal = jwtService.extractSubject(token);
        } catch (ExpiredJwtException | UnsupportedJwtException
            | MalformedJwtException | SignatureException ex) {
            throw new BadCredentialsException(ex.getMessage());
        }

        Account account = accountRepository.findById(principal.getId())
            .orElseThrow(() -> new BadCredentialsException("Account not found"));

        List<GrantedAuthority> authorities = List.of(account.getRole().asAuthority());
        return new UserPrincipalAuthenticationToken(principal, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == JwtAuthenticationToken.class;
    }
}
