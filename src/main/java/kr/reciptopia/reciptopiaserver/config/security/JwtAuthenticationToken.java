package kr.reciptopia.reciptopiaserver.config.security;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;

    public JwtAuthenticationToken(String token) {
        super(Collections.emptyList());
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String getPrincipal() {
        return getToken();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

}
