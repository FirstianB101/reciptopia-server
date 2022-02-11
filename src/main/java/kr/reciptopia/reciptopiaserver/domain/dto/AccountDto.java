package kr.reciptopia.reciptopiaserver.domain.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import lombok.Builder;
import lombok.Data;
import lombok.With;

public interface AccountDto {

    static Result of(Account entitiy) {
        return Result.builder()
            .email(entitiy.getEmail())
            .nickname(entitiy.getNickname())
            .profilePictureUrl(entitiy.getProfilePictureUrl())
            .build();
    }

    @Data
    @Builder
    @With
    class Create {

        @NotNull
        @Email(message = "이메일 형식이 아닙니다.")
        private String email;

        @NotEmpty
        @Size(min = 8, max = 16, message = "password는 8 ~ 16자 이여야 합니다!")
        private String password;

        @NotBlank
        @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
        private String nickname;
    }

    @Data
    @Builder
    @With
    class Update {

        @Email(message = "이메일 형식이 아닙니다.")
        private String email;

        @Size(min = 8, max = 16, message = "password는 8 ~ 16자 이여야 합니다!")
        private String password;

        @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
        private String nickname;

        private String profilePictureUrl;
    }

    @Data
    @Builder
    @With
    class Result {

        @NotNull
        @Email(message = "이메일 형식이 아닙니다.")
        private String email;

        @NotBlank
        @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
        private String nickname;

        private String profilePictureUrl;
    }

    @Data
    @Builder
    @With
    class CheckDuplicationResult {

        @NotNull
        private Boolean exists;
    }
}
