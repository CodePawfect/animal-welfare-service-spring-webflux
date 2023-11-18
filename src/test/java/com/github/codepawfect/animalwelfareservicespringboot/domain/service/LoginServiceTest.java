package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import java.util.Collection;
import com.github.codepawfect.animalwelfareservicespringboot.core.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

  @Mock ReactiveUserDetailsService reactiveUserDetailsService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @InjectMocks LoginService loginService;

  private static final String MOCK_USERNAME = "mock_username";
  private static final String MOCK_PASSWORD = "mock_password";

  @Test
  void authenticate_success_with_valid_credentials() {
    // Arrange
    when(reactiveUserDetailsService.findByUsername(MOCK_USERNAME))
        .thenReturn(Mono.just(createMockUserDetails()));
    when(passwordEncoder.matches(MOCK_PASSWORD, MOCK_PASSWORD)).thenReturn(true);
    when(jwtService.generateToken(MOCK_USERNAME)).thenReturn("testToken");

    // Act & Assert
    StepVerifier.create(loginService.authenticate(MOCK_USERNAME, MOCK_PASSWORD))
        .expectNext("testToken")
        .expectComplete()
        .verify();

    // Verify interactions
    verify(reactiveUserDetailsService, times(1)).findByUsername(MOCK_USERNAME);
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(jwtService, times(1)).generateToken(MOCK_USERNAME);
  }

  @Test
  void authenticate_returns_BadCredentialsException_with_invalid_credentials() {
    // Arrange
    when(reactiveUserDetailsService.findByUsername(MOCK_USERNAME))
        .thenReturn(Mono.just(createMockUserDetails()));
    when(passwordEncoder.matches(MOCK_PASSWORD, MOCK_PASSWORD)).thenReturn(false);

    // Act & Assert
    StepVerifier.create(loginService.authenticate(MOCK_USERNAME, MOCK_PASSWORD))
        .expectError(BadCredentialsException.class)
        .verify();

    // Verify interactions
    verify(reactiveUserDetailsService, times(1)).findByUsername(MOCK_USERNAME);
    verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    verify(jwtService, never()).generateToken(MOCK_USERNAME);
  }

  private UserDetails createMockUserDetails() {
    return new UserDetails() {
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
      }

      @Override
      public String getPassword() {
        return MOCK_PASSWORD;
      }

      @Override
      public String getUsername() {
        return MOCK_USERNAME;
      }

      @Override
      public boolean isAccountNonExpired() {
        return false;
      }

      @Override
      public boolean isAccountNonLocked() {
        return false;
      }

      @Override
      public boolean isCredentialsNonExpired() {
        return false;
      }

      @Override
      public boolean isEnabled() {
        return true;
      }
    };
  }
}
