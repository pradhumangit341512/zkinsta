package com.instagram.auth.dto;

import com.instagram.auth.validator.ValidFullName;
import com.instagram.auth.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Please provide a valid full name")
    @ValidFullName
    private String fullName;

    @NotBlank(message = "Please provide a valid email address")
    @Email(message = "Please provide a valid email address")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|org|in)$",
            message = "Email Address should be in a valid email format with domains like 'com' or 'org' or 'in'")
    private String email;

    @NotBlank(message = "Please provide a valid username")
    @Pattern(regexp = "^[a-z0-9._-]+$",
            message = "Username should contain only lowercase letters, digits, and special characters")
    private String username;

    @NotBlank(message = "Please provide a valid password")
    @ValidPassword
    private String password;

    @NotBlank(message = "Please provide a valid confirm password")
    private String confirmPassword;
}
