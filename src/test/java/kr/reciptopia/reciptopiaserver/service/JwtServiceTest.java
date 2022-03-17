package kr.reciptopia.reciptopiaserver.service;

import static kr.reciptopia.reciptopiaserver.helper.AccountHelper.anAccount;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import kr.reciptopia.reciptopiaserver.business.service.JwtService;
import kr.reciptopia.reciptopiaserver.config.security.UserPrincipal;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import org.junit.jupiter.api.Test;

public class JwtServiceTest {

    private static final String SECRET = "Z1VrWHAyczV2OHkvQj9FKEgrTWJRZVNoVm1ZcTN0Nnc=";
    private static final int EXP_INTERVAL = 60 * 60 * 24 * 7; // a week

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new ParameterNamesModule());

    private final JwtService jwtService = new JwtService(objectMapper, SECRET, EXP_INTERVAL);

    @Test
    void extractSubject() {
        // Given
        Account account = anAccount();
        String jwt = jwtService.signJwt(account);

        // When
        UserPrincipal principal = jwtService.extractSubject(jwt);

        // Then
        assertThat(principal.id())
            .isEqualTo(account.getId());

        assertThat(principal.email())
            .isEqualTo(account.getEmail());
    }
}
