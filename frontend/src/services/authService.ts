import api from './api';
import { ApiResponse, AuthResponse, RegisterRequest, LoginRequest, User } from '../types';

export const authService = {
  register: (data: RegisterRequest) =>
    api.post<ApiResponse<AuthResponse>>('/api/auth/register', data),

  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthResponse>>('/api/auth/login', data),

  getProfile: () =>
    api.get<ApiResponse<User>>('/api/auth/profile'),

  getProfileByUsername: (username: string) =>
    api.get<ApiResponse<User>>(`/api/auth/profile/${username}`),

  updateProfile: (data: { fullName?: string; bio?: string; profilePicture?: string }) =>
    api.put<ApiResponse<User>>('/api/auth/profile', data),

  forgotPassword: (email: string) =>
    api.post<ApiResponse<string>>('/api/auth/forgot-password', { email }),

  resetPassword: (data: { token: string; newPassword: string; confirmPassword: string }) =>
    api.post<ApiResponse<void>>('/api/auth/reset-password', data),

  searchUsers: (query: string) =>
    api.get<ApiResponse<User[]>>('/api/auth/users/search', { params: { query } }),

  checkUsername: (username: string) =>
    api.get<ApiResponse<boolean>>(`/api/auth/check-username/${encodeURIComponent(username)}`),

  checkEmail: (email: string) =>
    api.get<ApiResponse<boolean>>(`/api/auth/check-email/${encodeURIComponent(email)}`),
};
