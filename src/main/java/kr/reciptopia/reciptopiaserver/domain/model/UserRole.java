package kr.reciptopia.reciptopiaserver.domain.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
    USER, ADMIN;

    public GrantedAuthority asAuthority() {
        return new SimpleGrantedAuthority(this.name());
    }
}
