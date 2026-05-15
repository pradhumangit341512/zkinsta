package com.instagram.auth.service;

import com.instagram.auth.config.JwtUtil;
import com.instagram.auth.dto.*;
import com.instagram.auth.entity.User;
import com.instagram.auth.exception.CustomException;
import com.instagram.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .username("johndoe")
                .password("encodedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .username("johndoe")
                .password("Test@1234")
                .confirmPassword("Test@1234")
                .build();

        loginRequest = LoginRequest.builder()
                .username("johndoe")
                .password("Test@1234")
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), any(Long.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_PasswordMismatch() {
        registerRequest.setConfirmPassword("Different@123");

        assertThrows(CustomException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_UsernameExists() {
        registerRequest.setConfirmPassword("Test@1234");
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_EmailExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.register(registerRequest));
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Test@1234", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("johndoe", 1L)).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void login_InvalidUsername() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_InvalidPassword() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Test@1234", "encodedPassword")).thenReturn(false);

        assertThrows(CustomException.class, () -> authService.login(loginRequest));
    }

    @Test
    void getProfile_Success() {
        UserProfileDto profileDto = UserProfileDto.builder()
                .id(1L).fullName("John Doe").username("johndoe").build();
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserProfileDto.class)).thenReturn(profileDto);

        UserProfileDto result = authService.getProfile("johndoe");

        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
    }

    @Test
    void getProfile_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> authService.getProfile("unknown"));
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .fullName("John Updated").bio("New bio").build();
        UserProfileDto profileDto = UserProfileDto.builder()
                .id(1L).fullName("John Updated").username("johndoe").bio("New bio").build();

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(modelMapper.map(testUser, UserProfileDto.class)).thenReturn(profileDto);

        UserProfileDto result = authService.updateProfile("johndoe", updateRequest);

        assertNotNull(result);
        assertEquals("New bio", result.getBio());
    }

    @Test
    void forgotPassword_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String token = authService.forgotPassword("john@example.com");

        assertNotNull(token);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void forgotPassword_EmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> authService.forgotPassword("unknown@example.com"));
    }

    @Test
    void resetPassword_Success() {
        testUser.setPasswordResetToken("valid-token");
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

        ResetPasswordConfirm request = ResetPasswordConfirm.builder()
                .token("valid-token").newPassword("NewPass@123").confirmPassword("NewPass@123").build();

        when(userRepository.findByPasswordResetToken("valid-token")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("NewPass@123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.resetPassword(request);

        assertEquals("Password reset successful", result);
    }

    @Test
    void resetPassword_PasswordMismatch() {
        ResetPasswordConfirm request = ResetPasswordConfirm.builder()
                .token("valid-token").newPassword("NewPass@123").confirmPassword("Different@123").build();

        assertThrows(CustomException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_ExpiredToken() {
        testUser.setPasswordResetToken("expired-token");
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().minusHours(1));

        ResetPasswordConfirm request = ResetPasswordConfirm.builder()
                .token("expired-token").newPassword("NewPass@123").confirmPassword("NewPass@123").build();

        when(userRepository.findByPasswordResetToken("expired-token")).thenReturn(Optional.of(testUser));

        assertThrows(CustomException.class, () -> authService.resetPassword(request));
    }

    @Test
    void searchUsers_Success() {
        UserProfileDto profileDto = UserProfileDto.builder()
                .id(1L).fullName("John Doe").username("johndoe").build();
        when(userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase("john", "john"))
                .thenReturn(List.of(testUser));
        when(modelMapper.map(testUser, UserProfileDto.class)).thenReturn(profileDto);

        List<UserProfileDto> results = authService.searchUsers("john");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void getUserById_Success() {
        UserProfileDto profileDto = UserProfileDto.builder()
                .id(1L).fullName("John Doe").username("johndoe").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(modelMapper.map(testUser, UserProfileDto.class)).thenReturn(profileDto);

        UserProfileDto result = authService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> authService.getUserById(999L));
    }
}
