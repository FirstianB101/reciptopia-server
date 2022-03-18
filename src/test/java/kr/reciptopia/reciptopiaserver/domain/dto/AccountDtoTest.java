package kr.reciptopia.reciptopiaserver.domain.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountDtoTest {

    @Nested
    class Create {

        @Mock
        private PasswordEncoder passwordEncoder;

        @Test
        void asEntity() {
            // Given
            given(passwordEncoder.encode(any())).willReturn("encodedPassword");

            // When
            AccountDto.Create dto = AccountDto.Create.builder()
                .email("test@email.com")
                .password("this!sPassw0rd")
                .nickname("pte1024")
                .build();

            Account entity = dto.asEntity(it -> it
                .withRole(UserRole.USER)
                .withPassword(passwordEncoder::encode, "this!sPassw0rd")
            );

            // Then
            assertThat(entity.getEmail()).isEqualTo("test@email.com");
            assertThat(entity.getNickname()).isEqualTo("pte1024");
            assertThat(entity.getPassword()).isEqualTo("encodedPassword");
            assertThat(entity.getRole()).isEqualTo(UserRole.USER);
        }
    }

    @Nested
    class Result {

        @Test
        public void of() {
            // Given
            Account account = Account.builder()
                .email("test@email.com")
                .password("encodedPassword")
                .nickname("pte1024")
                .role(UserRole.USER).build();

            // When
            AccountDto.Result result = AccountDto.Result.of(account);

            // Then
            assertThat(result.email()).isEqualTo("test@email.com");
            assertThat(result.nickname()).isEqualTo("pte1024");
            assertThat(result.role()).isEqualTo(UserRole.USER);
        }

    }
}