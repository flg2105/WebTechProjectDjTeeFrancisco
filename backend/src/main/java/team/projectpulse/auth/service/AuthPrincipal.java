package team.projectpulse.auth.service;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team.projectpulse.user.domain.ProjectUser;
import team.projectpulse.user.domain.UserRole;
import team.projectpulse.user.domain.UserStatus;

public class AuthPrincipal implements UserDetails {
  private final Long userId;
  private final String email;
  private final String passwordHash;
  private final String displayName;
  private final UserRole role;
  private final UserStatus status;

  public AuthPrincipal(ProjectUser user) {
    this.userId = user.getId();
    this.email = user.getEmail();
    this.passwordHash = user.getPasswordHash();
    this.displayName = user.getDisplayName();
    this.role = user.getRole();
    this.status = user.getStatus();
  }

  public Long getUserId() {
    return userId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public UserRole getRole() {
    return role;
  }

  public UserStatus getStatus() {
    return status;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return status != UserStatus.INACTIVE;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return status == UserStatus.ACTIVE;
  }
}
