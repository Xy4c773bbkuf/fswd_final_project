package com.skillswap.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expirationHours;
  public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-hours:24}") long expirationHours) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirationHours = expirationHours;
  }
  public String issue(UUID userId, String email) {
    var now = Instant.now();
    return Jwts.builder().subject(userId.toString()).claim("email", email).issuedAt(Date.from(now))
      .expiration(Date.from(now.plusSeconds(expirationHours * 3600))).signWith(key).compact();
  }
  public UUID subject(String token) { return UUID.fromString(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject()); }
}
