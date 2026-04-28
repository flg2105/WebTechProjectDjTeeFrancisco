package team.projectpulse.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.projectpulse.user.domain.ProjectUser;

@Service
public class JwtService {
  private final SecretKey signingKey;
  private final long expirationSeconds;

  public JwtService(
      @Value("${app.security.jwt-secret}") String secret,
      @Value("${app.security.jwt-expiration-seconds}") long expirationSeconds) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationSeconds = expirationSeconds;
  }

  public String generateToken(ProjectUser user) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(expirationSeconds);
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("userId", user.getId())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .signWith(signingKey)
        .compact();
  }

  public Instant getExpirationInstant() {
    return Instant.now().plusSeconds(expirationSeconds);
  }

  public String extractUsername(String token) {
    return parseClaims(token).getSubject();
  }

  public boolean isTokenValid(String token, AuthPrincipal principal) {
    Claims claims = parseClaims(token);
    String subject = claims.getSubject();
    return subject != null
        && subject.equalsIgnoreCase(principal.getUsername())
        && claims.getExpiration() != null
        && claims.getExpiration().toInstant().isAfter(Instant.now());
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(signingKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException | IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid JWT token", ex);
    }
  }
}
