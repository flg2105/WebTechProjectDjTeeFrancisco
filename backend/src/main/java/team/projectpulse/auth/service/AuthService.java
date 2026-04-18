package team.projectpulse.auth.service;

import org.springframework.stereotype.Service;
import team.projectpulse.auth.dto.LoginRequest;
import team.projectpulse.auth.dto.LoginResponse;

@Service
public class AuthService {

  public LoginResponse login(LoginRequest request) {
    return new LoginResponse("Bearer", "placeholder");
  }
}

