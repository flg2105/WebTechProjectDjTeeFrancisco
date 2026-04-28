package team.projectpulse.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.auth.dto.AuthTokenResponse;
import team.projectpulse.auth.dto.CurrentUserResponse;
import team.projectpulse.auth.service.AuthPrincipal;
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
  public Result<AuthTokenResponse> login(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
    return Result.ok("Login Success", authService.login(authorizationHeader));
  }

  @GetMapping("/me")
  public Result<CurrentUserResponse> me(@AuthenticationPrincipal AuthPrincipal principal) {
    return Result.ok("Find Current User Success", authService.currentUser(principal));
  }
}
