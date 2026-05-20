package com.instagram.auth.dto;

import com.instagram.auth.validator.ValidFullName;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @ValidFullName
    @Size(max = 100, message = "Full name must be under 100 characters")
    private String fullName;

    @Size(max = 500, message = "Bio must be under 500 characters")
    private String bio;

    @Size(max = 500, message = "Profile picture URL must be under 500 characters")
    private String profilePicture;
}
