package com.instagram.auth.controller;

import com.instagram.auth.dto.*;
import com.instagram.auth.repository.UserRepository;
import com.instagram.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and profile management APIs")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success("Registration successful", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse> getProfile(Authentication authentication) {
        UserProfileDto profile = authService.getProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }

    @GetMapping("/profile/{username}")
    @Operation(summary = "Get user profile by username")
    public ResponseEntity<ApiResponse> getProfileByUsername(@PathVariable String username) {
        UserProfileDto profile = authService.getProfile(username);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse> updateProfile(Authentication authentication,
                                                     @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto profile = authService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "If an account exists with this email, a password reset link has been sent.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordConfirm request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    @GetMapping("/users/search")
    @Operation(summary = "Search users by username or full name")
    public ResponseEntity<ApiResponse> searchUsers(@RequestParam String query) {
        List<UserProfileDto> users = authService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        UserProfileDto profile = authService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", profile));
    }

    @GetMapping("/check-username/{username}")
    @Operation(summary = "Check if username is available")
    public ResponseEntity<ApiResponse> checkUsername(@PathVariable String username) {
        boolean available = !userRepository.existsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("Availability checked", available));
    }

    @GetMapping("/check-email/{email}")
    @Operation(summary = "Check if email is available")
    public ResponseEntity<ApiResponse> checkEmail(@PathVariable String email) {
        boolean available = !userRepository.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Availability checked", available));
    }
}
