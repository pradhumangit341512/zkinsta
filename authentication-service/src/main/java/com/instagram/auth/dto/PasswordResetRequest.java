package com.instagram.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequest {

    @NotBlank(message = "Please provide a valid email")
    @Email(message = "Please provide a valid email")
    private String email;
}
