import api from './api';
import { ApiResponse, Notification, PageResponse } from '../types';

export const notificationService = {
  getNotifications: (page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Notification>>>('/api/notifications', { params: { page, size } }),

  getUnreadCount: () =>
    api.get<ApiResponse<number>>('/api/notifications/unread-count'),

  markAsRead: (notificationId: number) =>
    api.put<ApiResponse<void>>(`/api/notifications/${notificationId}/read`),

  markAllAsRead: () =>
    api.put<ApiResponse<void>>('/api/notifications/read-all'),
};
