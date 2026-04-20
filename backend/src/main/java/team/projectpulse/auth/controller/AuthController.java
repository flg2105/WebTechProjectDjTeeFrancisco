package team.projectpulse.auth.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.auth.dto.LoginRequest;
import team.projectpulse.auth.dto.LoginResponse;
import team.projectpulse.auth.service.AuthService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return Result.ok("Login scaffold (no enforcement yet)", authService.login(request));
  }

  @GetMapping("/me")
  public Result<Void> me() {
    return Result.ok("Me scaffold (no enforcement yet)", null);
  }
}

