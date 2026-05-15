import api from './api';
import { ApiResponse, FollowDto, FollowCount } from '../types';

export const followService = {
  followUser: (followingId: number, followingUsername: string) =>
    api.post<ApiResponse<FollowDto>>('/api/follows', { followingId, followingUsername }),

  unfollowUser: (followingId: number) =>
    api.delete<ApiResponse<void>>(`/api/follows/${followingId}`),

  getFollowers: (userId: number) =>
    api.get<ApiResponse<FollowDto[]>>(`/api/follows/followers/${userId}`),

  getFollowing: (userId: number) =>
    api.get<ApiResponse<FollowDto[]>>(`/api/follows/following/${userId}`),

  getFollowCounts: (userId: number) =>
    api.get<ApiResponse<FollowCount>>(`/api/follows/count/${userId}`),

  isFollowing: (followingId: number) =>
    api.get<ApiResponse<boolean>>('/api/follows/check', { params: { followingId } }),

  getFollowingIds: (userId: number) =>
    api.get<ApiResponse<number[]>>(`/api/follows/following-ids/${userId}`),
};
