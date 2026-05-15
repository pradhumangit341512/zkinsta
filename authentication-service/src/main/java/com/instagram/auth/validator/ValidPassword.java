package com.instagram.auth.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password should contain lowercase, uppercase, digits, and special characters at least one each. Min length 8, max 16";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
