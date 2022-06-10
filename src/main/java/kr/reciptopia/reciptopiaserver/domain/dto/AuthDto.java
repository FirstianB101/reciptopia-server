package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;

public interface AuthDto {

    record GenerateToken(
        @NotEmpty @Email String email,
        @NotEmpty @Size(min = 8, max = 16, message = "password는 8 ~ 16자 이여야 합니다!") String password) {

        @Builder
        public GenerateToken {

        }
    }

    record GenerateTokenResult(
        @NotEmpty String token,
        @NotNull AccountDto.Result account) {

        @Builder
        public GenerateTokenResult {

        }
    }

    record MeResult(@NotNull AccountDto.Result account) {

        @Builder
        public MeResult {

        }
    }
}
