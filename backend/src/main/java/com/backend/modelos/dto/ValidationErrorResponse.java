package com.backend.modelos.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrorResponse {
    private final List<String> errors;

    public ValidationErrorResponse() {
        this.errors = new ArrayList<>();
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
