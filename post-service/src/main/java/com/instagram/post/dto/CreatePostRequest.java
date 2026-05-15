package com.instagram.post.dto;

import com.instagram.post.entity.Post;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {

    private String caption;

    private String mediaUrl;

    @NotNull(message = "Please provide a valid media type")
    private Post.MediaType mediaType;

    private Post.Privacy privacy;

    private String filter;

    private List<String> hashtags;
}
