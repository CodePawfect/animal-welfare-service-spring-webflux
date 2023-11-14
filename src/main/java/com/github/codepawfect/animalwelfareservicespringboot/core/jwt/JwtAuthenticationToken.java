package com.github.codepawfect.animalwelfareservicespringboot.core.jwt;

import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
  private final String token;

  public JwtAuthenticationToken(String token) {
    super(AuthorityUtils.NO_AUTHORITIES);
    this.token = token;
  }

  @Override
  public String getCredentials() {
    return this.token;
  }

  @Override
  public String getPrincipal() {
    return this.token;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    JwtAuthenticationToken that = (JwtAuthenticationToken) o;

    return Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (token != null ? token.hashCode() : 0);
    return result;
  }
}
