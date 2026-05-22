package com.uees.studentservices.security;

import com.uees.studentservices.config.JwtProperties;
import com.uees.studentservices.model.Student;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = buildKey(props.getSecret());
    }

    private SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.security.jwt.secret no configurado");
        }
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(Student student) {
        return buildToken(student, TOKEN_TYPE_ACCESS, props.getAccessTokenExpirationMs());
    }

    public String generateRefreshToken(Student student) {
        return buildToken(student, TOKEN_TYPE_REFRESH, props.getRefreshTokenExpirationMs());
    }

    private String buildToken(Student student, String type, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", type);
        claims.put("email", student.getEmail());
        claims.put("name", student.getFullName());
        claims.put("role", student.getRole().name());
        claims.put("sid", student.getId().toString());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(student.getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public long getAccessTokenExpirationSeconds() {
        return props.getAccessTokenExpirationMs() / 1000L;
    }

    public UUID extractStudentId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    public String extractEmail(String token) {
        return extractClaim(token, c -> c.get("email", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, c -> c.get("role", String.class));
    }

    public String extractTokenType(String token) {
        return extractClaim(token, c -> c.get("type", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseAll(token));
    }

    private Claims parseAll(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
