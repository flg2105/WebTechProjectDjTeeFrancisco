package com.projectpulse.backend.controller;

import com.projectpulse.backend.dto.InvitationLookupResponse;
import com.projectpulse.backend.dto.InstructorRegistrationRequest;
import com.projectpulse.backend.dto.RegistrationCompleteResponse;
import com.projectpulse.backend.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/{token}")
    public InvitationLookupResponse lookupInvitation(@PathVariable String token) {
        return registrationService.lookupInvitation(token);
    }

    @PostMapping("/instructor")
    public RegistrationCompleteResponse registerInstructor(@Valid @RequestBody InstructorRegistrationRequest request) {
        return registrationService.registerInstructor(request);
    }
}
