package com.instagram.post.dto;

import com.instagram.post.entity.Post;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {

    @Size(max = 2000, message = "Caption must be under 2000 characters")
    private String caption;

    private Post.Privacy privacy;

    @Size(max = 100, message = "Filter name must be under 100 characters")
    private String filter;

    @Size(max = 30, message = "Maximum 30 hashtags allowed")
    private List<@Size(max = 100, message = "Each hashtag must be under 100 characters") String> hashtags;
}
