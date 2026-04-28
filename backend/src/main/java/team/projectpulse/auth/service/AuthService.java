package team.projectpulse.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.auth.dto.AuthTokenResponse;
import team.projectpulse.auth.dto.CurrentUserResponse;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserStatus;
import team.projectpulse.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public AuthTokenResponse login(String authorizationHeader) {
    BasicCredentials credentials = parseBasicCredentials(authorizationHeader);
    ProjectUser user = userRepository.findByEmailIgnoreCase(credentials.username())
        .orElseThrow(() -> new ApiException(StatusCode.UNAUTHORIZED, "Invalid credentials"));

    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new ApiException(StatusCode.LOCKED, "Account is not active");
    }
    if (user.getPasswordHash() == null || !passwordEncoder.matches(credentials.password(), user.getPasswordHash())) {
      throw new ApiException(StatusCode.UNAUTHORIZED, "Invalid credentials");
    }

    String token = jwtService.generateToken(user);
    Instant expiresAt = jwtService.getExpirationInstant();
    return new AuthTokenResponse(
        "Bearer",
        token,
        expiresAt,
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        user.getRole(),
        user.getStatus());
  }

  public CurrentUserResponse currentUser(AuthPrincipal principal) {
    return new CurrentUserResponse(
        principal.getUserId(),
        principal.getUsername(),
        principal.getDisplayName(),
        principal.getRole(),
        principal.getStatus());
  }

  private BasicCredentials parseBasicCredentials(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
      throw new ApiException(StatusCode.UNAUTHORIZED, "Basic authorization is required");
    }

    try {
      String encoded = authorizationHeader.substring(6).trim();
      String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
      int colonIndex = decoded.indexOf(':');
      if (colonIndex <= 0 || colonIndex == decoded.length() - 1) {
        throw new ApiException(StatusCode.UNAUTHORIZED, "Invalid basic authorization header");
      }
      String username = decoded.substring(0, colonIndex).trim().toLowerCase();
      String password = decoded.substring(colonIndex + 1);
      return new BasicCredentials(username, password);
    } catch (IllegalArgumentException ex) {
      throw new ApiException(StatusCode.UNAUTHORIZED, "Invalid basic authorization header");
    }
  }

  private record BasicCredentials(String username, String password) {}
}
