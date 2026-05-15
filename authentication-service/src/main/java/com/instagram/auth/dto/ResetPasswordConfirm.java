package com.instagram.auth.dto;

import com.instagram.auth.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordConfirm {

    @NotBlank(message = "Please provide a valid token")
    private String token;

    @NotBlank(message = "Please provide a valid new password")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Please provide a valid confirm password")
    private String confirmPassword;
}
