package com.instagram.auth.service;

import com.instagram.auth.config.JwtUtil;
import com.instagram.auth.dto.*;
import com.instagram.auth.entity.User;
import com.instagram.auth.exception.CustomException;
import com.instagram.auth.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    @CircuitBreaker(name = "authService", fallbackMethod = "registerFallback")
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("Username already exists", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already exists", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .message("Registration successful")
                .build();
    }

    public AuthResponse registerFallback(RegisterRequest request, Throwable t) {
        if (t instanceof CustomException) {
            throw (CustomException) t;
        }
        log.error("Circuit breaker fallback for register: {}", t.getMessage());
        throw new CustomException("Service is temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "loginFallback")
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("Invalid username or password", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .message("Login successful")
                .build();
    }

    public AuthResponse loginFallback(LoginRequest request, Throwable t) {
        if (t instanceof CustomException) {
            throw (CustomException) t;
        }
        log.error("Circuit breaker fallback for login: {}", t.getMessage());
        throw new CustomException(
                "Too many failed login attempts. Please wait for 1 minute before trying again.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    public UserProfileDto getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        return modelMapper.map(user, UserProfileDto.class);
    }

    public UserProfileDto updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        userRepository.save(user);
        return modelMapper.map(user, UserProfileDto.class);
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("No account found with this email", HttpStatus.NOT_FOUND));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        log.info("Password reset token generated for user: {}", user.getUsername());
        return token;
    }

    public String resetPassword(ResetPasswordConfirm request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new CustomException("Invalid or expired reset token", HttpStatus.BAD_REQUEST));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new CustomException("Reset token has expired", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        return "Password reset successful";
    }

    public List<UserProfileDto> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query)
                .stream()
                .map(user -> modelMapper.map(user, UserProfileDto.class))
                .collect(Collectors.toList());
    }

    public UserProfileDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        return modelMapper.map(user, UserProfileDto.class);
    }
}
