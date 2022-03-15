package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface AccountDto {

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

        public Account asEntity() {
            return Account.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .build();
        }
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
        private Long id;

        @NotNull
        @Email(message = "이메일 형식이 아닙니다.")
        private String email;

        @NotBlank
        @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
        private String nickname;

        private String profilePictureUrl;

        @NotEmpty
        private UserRole role;

        public static Result of(Account entitiy) {
            return Result.builder()
                .id(entitiy.getId())
                .email(entitiy.getEmail())
                .nickname(entitiy.getNickname())
                .profilePictureUrl(entitiy.getProfilePictureUrl())
                .role(entitiy.getRole())
                .build();
        }

        public static List<Result> of(Streamable<Account> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }

    @Data
    @Builder
    @With
    class CheckDuplicationResult {

        @NotNull
        private Boolean exists;
    }
}
