package team.projectpulse.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.repository.UserRepository;

@Service
public class AuthUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public AuthUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    ProjectUser user = userRepository.findByEmailIgnoreCase(username)
        .orElseThrow(() -> new ApiException(StatusCode.UNAUTHORIZED, "Invalid credentials"));
    return new AuthPrincipal(user);
  }
}
