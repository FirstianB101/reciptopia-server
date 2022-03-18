package kr.reciptopia.reciptopiaserver.config.security;

import lombok.Builder;

public record UserPrincipal(Long id, String email) {

    @Builder
    public UserPrincipal {
    }
}
