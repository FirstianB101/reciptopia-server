package kr.reciptopia.reciptopiaserver.controller;

import javax.validation.Valid;
import kr.reciptopia.reciptopiaserver.business.service.AuthTokenService;
import kr.reciptopia.reciptopiaserver.domain.dto.AuthDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class AuthController {

    private final AuthTokenService authTokenService;

    @Autowired
    public AuthController(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @PostMapping("/auth/token")
    public AuthDto.GenerateTokenResult generateToken(
        @Valid @RequestBody AuthDto.GenerateToken dto) {
        return authTokenService.generateToken(dto);
    }

    @GetMapping("/auth/me")
    public AuthDto.MeResult me(Authentication authentication) {
        return authTokenService.getMe(authentication);
    }

}
