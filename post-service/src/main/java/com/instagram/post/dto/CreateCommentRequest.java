package com.instagram.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment must be under 2000 characters")
    private String text;
}
