package com.instagram.auth.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }
        if (password.length() < 8 || password.length() > 16) {
            return false;
        }
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return hasLower && hasUpper && hasDigit && hasSpecial;
    }
}
