package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public interface AuthDto {

    @Data
    @Builder
    class GenerateToken {

        @NotEmpty
        @Email
        private String email;

        @NotEmpty
        private String password;

    }

    @Data
    @Builder
    class GenerateTokenResult {

        @NotEmpty
        private String token;

        @NotNull
        private AccountDto.Result account;

    }

    @Data
    @Builder
    class MeResult {

        @NotNull
        private AccountDto.Result account;
    }
}
