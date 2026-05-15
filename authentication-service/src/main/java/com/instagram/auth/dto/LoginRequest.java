package com.instagram.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Please provide a valid username")
    private String username;

    @NotBlank(message = "Please provide a valid password")
    private String password;
}
