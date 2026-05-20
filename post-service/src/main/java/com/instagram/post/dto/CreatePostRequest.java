package com.instagram.post.dto;

import com.instagram.post.entity.Post;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {

    @Size(max = 2000, message = "Caption must be under 2000 characters")
    private String caption;

    @Size(max = 500, message = "Media URL must be under 500 characters")
    private String mediaUrl;

    @NotNull(message = "Please provide a valid media type")
    private Post.MediaType mediaType;

    private Post.Privacy privacy;

    @Size(max = 100, message = "Filter name must be under 100 characters")
    private String filter;

    @Size(max = 30, message = "Maximum 30 hashtags allowed")
    private List<@Size(max = 100, message = "Each hashtag must be under 100 characters") String> hashtags;
}

