package com.instagram.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Please provide a valid username")
    @Size(max = 100, message = "Username must be under 100 characters")
    private String username;

    @NotBlank(message = "Please provide a valid password")
    @Size(max = 128, message = "Password must be under 128 characters")
    private String password;
}
