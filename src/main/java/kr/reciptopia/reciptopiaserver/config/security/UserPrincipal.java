package kr.reciptopia.reciptopiaserver.config.security;

import lombok.Builder;

@Builder
public record UserPrincipal(Long id, String email) {

}
