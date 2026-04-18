package com.projectpulse.backend.dto;

public record InvitationLookupResponse(
        String token,
        String role,
        String email,
        String firstName,
        String lastName,
        boolean alreadyRegistered
) {
}
