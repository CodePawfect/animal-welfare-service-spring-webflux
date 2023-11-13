package com.github.codepawfect.animalwelfareservicespringboot.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class LoginService {

  private final UserDetails adminUser;
  private final PasswordEncoder passwordEncoder;

  public Mono<UserDetails> authenticate(String username, String password) {
    return Mono.justOrEmpty(adminUser)
        .filter(user -> user.getUsername().equals(username) &&
            passwordEncoder.matches(password, user.getPassword()))
        .switchIfEmpty(Mono.empty());
  }
}
