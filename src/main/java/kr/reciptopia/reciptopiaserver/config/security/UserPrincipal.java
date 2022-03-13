package kr.reciptopia.reciptopiaserver.config.security;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserPrincipal {

    private final Long id;
    private final String email;

    @Builder
    public UserPrincipal(Long id, String email) {
        this.id = id;
        this.email = email;
    }

}
