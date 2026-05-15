package com.instagram.post.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse {
    private String message;
    private boolean success;
    private LocalDateTime timestamp;
    private Object data;

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .message(message).success(true).timestamp(LocalDateTime.now()).data(data).build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .message(message).success(false).timestamp(LocalDateTime.now()).build();
    }
}
