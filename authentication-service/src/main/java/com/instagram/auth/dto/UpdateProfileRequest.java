package com.instagram.auth.dto;

import com.instagram.auth.validator.ValidFullName;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @ValidFullName
    private String fullName;

    private String bio;

    private String profilePicture;
}
