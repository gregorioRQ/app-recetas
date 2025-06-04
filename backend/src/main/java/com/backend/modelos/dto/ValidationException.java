package com.backend.modelos.dto;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final ValidationErrorResponse validationErrors;

    public ValidationException(String message, ValidationErrorResponse validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
}
