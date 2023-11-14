package com.github.codepawfect.animalwelfareservicespringboot.core.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  private JwtParser jwtParser;
  private SecretKey key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
  }

  public String generateToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(java.util.Date.from(Instant.now()))
        .setExpiration(java.util.Date.from(Instant.now().plusSeconds(900)))
        .signWith(key)
        .compact();
  }

  public String getUsername(String token) {
    return jwtParser.parseClaimsJws(token).getBody().getSubject();
  }

  public boolean validate(UserDetails user, String token) {
    return user.getUsername().equals(getUsername(token)) && !isExpired(token);
  }

  public boolean isExpired(String token) {
    return jwtParser
        .parseClaimsJws(token)
        .getBody()
        .getExpiration()
        .before(java.util.Date.from(Instant.now()));
  }
}
