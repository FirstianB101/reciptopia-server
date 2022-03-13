package kr.reciptopia.reciptopiaserver.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import kr.reciptopia.reciptopiaserver.config.security.UserPrincipal;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String JWT_ISSUER = "reciptopia-server";
    private final ObjectMapper objectMapper;
    private final SecretKey signingKey;
    private final int expirationInterval;
    private final JwtParser jwtParser;

    @Autowired
    public JwtService(ObjectMapper objectMapper,
        @Value("${auth.jwt.key.secret-string}") String secretString,
        @Value("${auth.jwt.expiration-interval}") int expirationInterval) {
        this.objectMapper = objectMapper;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretString));
        this.expirationInterval = expirationInterval;
        this.jwtParser = Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build();
    }

    @SneakyThrows
    public String signJwt(Account account) {
        UserPrincipal principal = UserPrincipal.builder()
            .id(account.getId())
            .email(account.getEmail())
            .build();

        String subject = objectMapper.writeValueAsString(principal);

        Instant now = Instant.now();
        Date nowDate = Date.from(now);
        Date expDate = Date.from(now.plusSeconds(expirationInterval));

        return Jwts.builder()
            .setIssuer(JWT_ISSUER)
            .setSubject(subject)
            .setExpiration(expDate)
            .setIssuedAt(nowDate)
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    @SneakyThrows
    public UserPrincipal extractSubject(String jwt)
        throws ExpiredJwtException, UnsupportedJwtException,
        MalformedJwtException, SignatureException {
        String subject = jwtParser.parseClaimsJws(jwt)
            .getBody()
            .getSubject();
        return objectMapper.readValue(subject, UserPrincipal.class);
    }

}
