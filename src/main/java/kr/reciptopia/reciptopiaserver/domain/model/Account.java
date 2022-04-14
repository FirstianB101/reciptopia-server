package kr.reciptopia.reciptopiaserver.domain.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.function.Function;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@With
@Entity
public class Account extends TimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @NotNull
    @Column(unique = true)
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @NotEmpty
    private String password;

    @NotBlank
    @Size(min = 5, max = 16, message = "nickname은 5 ~ 16자 이여야 합니다!")
    private String nickname;

    private String profilePictureUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public Account(String email, String password, String nickname,
        UserRole role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public void setPassword(Function<? super CharSequence, ? extends String> encoder,
        String password) {
        this.password = encoder.apply(password);
    }

    public Account withPassword(Function<? super CharSequence, ? extends String> encoder) {
        return withPassword(encoder, this.password);
    }

    public Account withPassword(Function<? super CharSequence, ? extends String> encoder,
        String password) {
        String encodedPassword = encoder.apply(password);
        return encodedPassword.equals(password) ? this : Account.builder()
            .email(email)
            .password(encodedPassword)
            .nickname(nickname)
            .role(role)
            .build()
            .withId(id)
            .withProfilePictureUrl(profilePictureUrl);
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

}
