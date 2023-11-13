package com.github.codepawfect.animalwelfareservicespringboot.domain.controller;

import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.LoginRequestResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.controller.model.LoginResponseResource;
import com.github.codepawfect.animalwelfareservicespringboot.domain.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Controller
public class LoginController {

  private final LoginService loginService;

  @PostMapping("/login")
  public Mono<ResponseEntity<LoginResponseResource>> login(@RequestBody LoginRequestResource loginRequestResource) {
    //return loginService.authenticate(loginRequestResource.username(), loginRequestResource.password())
    throw new UnsupportedOperationException("not implemented yet");
  }
}
