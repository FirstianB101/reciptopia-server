package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;

public interface AuthDto {

    record GenerateToken(
        @NotEmpty @Email String email, @NotEmpty String password) {

        @Builder
        public GenerateToken {

        }
    }

    record GenerateTokenResult(
        String token, AccountDto.Result account) {

        @Builder
        public GenerateTokenResult(
            @NotEmpty
                String token,

            @NotNull
                AccountDto.Result account) {
            this.token = token;
            this.account = account;
        }
    }

    record MeResult(AccountDto.Result account) {

        @Builder
        public MeResult(
            @NotNull AccountDto.Result account) {
            this.account = account;
        }
    }
}
