package com.instagram.auth.dto;

import com.instagram.auth.validator.ValidFullName;
import com.instagram.auth.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Please provide a valid full name")
    @ValidFullName
    @Size(max = 100, message = "Full name must be under 100 characters")
    private String fullName;

    @NotBlank(message = "Please provide a valid email address")
    @Email(message = "Please provide a valid email address")
    @Size(max = 254, message = "Email must be under 254 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|org|in)$",
            message = "Email Address should be in a valid email format with domains like 'com' or 'org' or 'in'")
    private String email;

    @NotBlank(message = "Please provide a valid username")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-z0-9._-]+$",
            message = "Username should contain only lowercase letters, digits, and special characters")
    private String username;

    @NotBlank(message = "Please provide a valid password")
    @Size(max = 128, message = "Password must be under 128 characters")
    @ValidPassword
    private String password;

    @NotBlank(message = "Please provide a valid confirm password")
    @Size(max = 128, message = "Confirm password must be under 128 characters")
    private String confirmPassword;
}
