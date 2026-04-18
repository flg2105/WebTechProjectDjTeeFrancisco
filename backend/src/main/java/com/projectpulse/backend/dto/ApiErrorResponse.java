package com.projectpulse.backend.dto;

import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        List<FieldValidationError> fieldErrors
) {
    public record FieldValidationError(String field, String message) {
    }
}
