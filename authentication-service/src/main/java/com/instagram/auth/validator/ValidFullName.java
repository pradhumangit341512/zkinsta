package com.instagram.auth.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FullNameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFullName {
    String message() default "Full Name should contain only letters and first letter of each word should be capitalized";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
