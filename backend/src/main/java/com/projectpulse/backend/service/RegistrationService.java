package com.projectpulse.backend.service;

import com.projectpulse.backend.dto.InstructorRegistrationRequest;
import com.projectpulse.backend.dto.InvitationLookupResponse;
import com.projectpulse.backend.dto.RegistrationCompleteResponse;
import com.projectpulse.backend.exception.ApiException;
import com.projectpulse.backend.user.domain.AccountStatus;
import com.projectpulse.backend.user.domain.RegistrationInvitation;
import com.projectpulse.backend.user.domain.UserAccount;
import com.projectpulse.backend.user.domain.UserRole;
import com.projectpulse.backend.user.repository.RegistrationInvitationRepository;
import com.projectpulse.backend.user.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class RegistrationService {

    private final RegistrationInvitationRepository invitationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            RegistrationInvitationRepository invitationRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.invitationRepository = invitationRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public InvitationLookupResponse lookupInvitation(String token) {
        RegistrationInvitation invitation = findInvitationOrThrow(token);

        if (invitation.getRole() != UserRole.INSTRUCTOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "This registration link is not for an instructor account.");
        }

        if (invitation.isExpired()) {
            throw new ApiException(HttpStatus.GONE, "INVITATION_EXPIRED", "This registration link has expired.");
        }

        boolean alreadyRegistered = invitation.isUsed() || userAccountRepository.existsByEmailIgnoreCase(invitation.getEmail());

        return new InvitationLookupResponse(
                invitation.getToken(),
                invitation.getRole().name(),
                invitation.getEmail(),
                invitation.getFirstName(),
                invitation.getLastName(),
                alreadyRegistered
        );
    }

    @Transactional
    public RegistrationCompleteResponse registerInstructor(InstructorRegistrationRequest request) {
        RegistrationInvitation invitation = findInvitationOrThrow(request.token());

        if (invitation.getRole() != UserRole.INSTRUCTOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "This registration link is not for an instructor account.");
        }

        if (invitation.isExpired()) {
            throw new ApiException(HttpStatus.GONE, "INVITATION_EXPIRED", "This registration link has expired.");
        }

        if (invitation.isUsed() || userAccountRepository.existsByEmailIgnoreCase(invitation.getEmail())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "ACCOUNT_ALREADY_REGISTERED",
                    "This instructor has already set up the account and should log in."
            );
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "PASSWORD_MISMATCH",
                    "Reentered password must match the password."
            );
        }

        UserAccount account = new UserAccount();
        account.setRole(UserRole.INSTRUCTOR);
        account.setFirstName(request.firstName().trim());
        account.setMiddleInitial(normalizeMiddleInitial(request.middleInitial()));
        account.setLastName(request.lastName().trim());
        account.setEmail(invitation.getEmail().trim().toLowerCase());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(account);

        invitation.setUsedAt(OffsetDateTime.now());
        invitationRepository.save(invitation);

        return new RegistrationCompleteResponse("Instructor account created successfully.", "/login");
    }

    private RegistrationInvitation findInvitationOrThrow(String token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "INVITATION_NOT_FOUND",
                        "We could not find a matching registration invitation."
                ));
    }

    private String normalizeMiddleInitial(String middleInitial) {
        if (middleInitial == null || middleInitial.isBlank()) {
            return null;
        }
        return middleInitial.trim().toUpperCase();
    }
}
