package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import lombok.Builder;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface AccountDto {

    @With
    record Create(
        String email, String password, String nickname) {

        @Builder
        public Create(
            @NotNull
            @Email(message = "이메일 형식이 아닙니다.")
                String email,

            @NotEmpty
            @Size(min = 8, max = 16, message = "password는 8 ~ 16자 이여야 합니다!")
                String password,

            @NotBlank
            @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
                String nickname) {
            this.email = email;
            this.password = password;
            this.nickname = nickname;
        }

        public Account asEntity(
            Function<? super Account, ? extends Account> initialize) {
            return initialize.apply(Account.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .build());
        }

        public Account asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Update(
        String email, String password, String nickname, String profilePictureUrl) {

        @Builder
        public Update(
            @Email(message = "이메일 형식이 아닙니다.")
                String email,

            @Size(min = 8, max = 16, message = "password는 8 ~ 16자 이여야 합니다!")
                String password,

            @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
                String nickname,

            String profilePictureUrl) {
            this.email = email;
            this.password = password;
            this.nickname = nickname;
            this.profilePictureUrl = profilePictureUrl;
        }
    }

    @With
    record Result(
        Long id, String email, String nickname, String profilePictureUrl, UserRole role) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
            @Email(message = "이메일 형식이 아닙니다.")
                String email,

            @NotBlank
            @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
                String nickname,

            String profilePictureUrl,

            @NotEmpty
                UserRole role) {
            this.id = id;
            this.email = email;
            this.nickname = nickname;
            this.profilePictureUrl = profilePictureUrl;
            this.role = role;
        }

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

    @With
    record CheckDuplicationResult(Boolean exists) {

        @Builder
        public CheckDuplicationResult(@NotNull Boolean exists) {
            this.exists = exists;
        }
    }
}
