package com.projectpulse.backend.user.repository;

import com.projectpulse.backend.user.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
