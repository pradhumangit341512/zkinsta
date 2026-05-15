package com.instagram.trending.exception;

import com.instagram.trending.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException ex) {
        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        return new ResponseEntity<>(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
