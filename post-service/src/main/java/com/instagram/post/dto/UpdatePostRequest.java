package com.instagram.post.dto;

import com.instagram.post.entity.Post;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {
    private String caption;
    private Post.Privacy privacy;
    private String filter;
    private List<String> hashtags;
}
