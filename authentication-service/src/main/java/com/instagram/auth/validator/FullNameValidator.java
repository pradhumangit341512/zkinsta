package com.instagram.auth.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FullNameValidator implements ConstraintValidator<ValidFullName, String> {

    @Override
    public boolean isValid(String fullName, ConstraintValidatorContext context) {
        if (fullName == null || fullName.isBlank()) {
            return true;
        }
        String[] words = fullName.trim().split("\\s+");
        if (words.length < 1) {
            return false;
        }
        for (String word : words) {
            if (!word.matches("^[A-Z][a-zA-Z]*$")) {
                return false;
            }
        }
        return true;
    }
}
