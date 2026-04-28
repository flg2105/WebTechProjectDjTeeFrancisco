package team.projectpulse.auth.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import team.projectpulse.system.ApiException;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.repository.UserRepository;

@Component("currentUserSecurity")
public class CurrentUserSecurity {
  private final UserRepository userRepository;

  public CurrentUserSecurity(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public boolean canEditAccount(Long userId) {
    if (isAdmin()) {
      return true;
    }
    return isCurrentUser(userId);
  }

  public boolean isCurrentUser(Long userId) {
    if (userId == null) {
      return false;
    }
    return currentUser().getId().equals(userId);
  }

  public boolean isAdmin() {
    return hasRole(UserRole.ADMIN);
  }

  public boolean isInstructor() {
    return hasRole(UserRole.INSTRUCTOR);
  }

  public boolean isStudent() {
    return hasRole(UserRole.STUDENT);
  }

  public Long currentUserId() {
    return currentUser().getId();
  }

  public UserRole currentRole() {
    if (hasRole(UserRole.ADMIN)) {
      return UserRole.ADMIN;
    }
    if (hasRole(UserRole.INSTRUCTOR)) {
      return UserRole.INSTRUCTOR;
    }
    return currentUser().getRole();
  }

  public void requireCurrentUser(Long userId) {
    if (!isCurrentUser(userId)) {
      throw new ApiException(StatusCode.FORBIDDEN, "You can only access your own records");
    }
  }

  private ProjectUser currentUser() {
    Authentication authentication = authentication();

    String username = authentication.getName();
    return userRepository.findByEmailIgnoreCase(username)
        .orElseThrow(() -> new ApiException(StatusCode.UNAUTHORIZED, "Authenticated user not found"));
  }

  private boolean hasRole(UserRole role) {
    return authentication().getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(authority -> authority.equals("ROLE_" + role.name()));
  }

  private Authentication authentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw new ApiException(StatusCode.UNAUTHORIZED, "Authentication required");
    }
    return authentication;
  }
}
