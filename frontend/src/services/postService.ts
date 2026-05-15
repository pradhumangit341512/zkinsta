import api from './api';
import { ApiResponse, Post, CreatePostRequest, PageResponse, Comment } from '../types';

export const postService = {
  uploadMedia: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<ApiResponse<{ mediaUrl: string }>>('/api/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  createPost: (data: CreatePostRequest) =>
    api.post<ApiResponse<Post>>('/api/posts', data),

  getPost: (postId: number) =>
    api.get<ApiResponse<Post>>(`/api/posts/${postId}`),

  getPostsByUser: (userId: number) =>
    api.get<ApiResponse<Post[]>>(`/api/posts/user/${userId}`),

  getPublicFeed: (page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Post>>>('/api/posts/public', { params: { page, size } }),

  getFeed: (followingIds: number[], page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Post>>>('/api/posts/feed', {
      params: { followingIds: followingIds.join(','), page, size },
    }),

  updatePost: (postId: number, data: Partial<CreatePostRequest>) =>
    api.put<ApiResponse<Post>>(`/api/posts/${postId}`, data),

  deletePost: (postId: number) =>
    api.delete<ApiResponse<void>>(`/api/posts/${postId}`),

  likePost: (postId: number) =>
    api.post<ApiResponse<Post>>(`/api/posts/${postId}/like`),

  unlikePost: (postId: number) =>
    api.delete<ApiResponse<Post>>(`/api/posts/${postId}/like`),

  getTrendingPosts: (page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Post>>>('/api/posts/trending', { params: { page, size } }),

  getRecentTrendingPosts: (page = 0, size = 20, timeFilter: '24h' | '7d' = '24h') =>
    api.get<ApiResponse<PageResponse<Post>>>('/api/posts/trending/recent', {
      params: { page, size, hours: timeFilter === '24h' ? 24 : 168 },
    }),

  getPostsByHashtag: (hashtag: string, page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Post>>>(`/api/posts/hashtag/${hashtag}`, { params: { page, size } }),

  searchPosts: (query: string, page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Post>>>('/api/posts/search', { params: { query, page, size } }),

  recordView: (postId: number) =>
    api.post<ApiResponse<Post>>(`/api/posts/${postId}/view`),

  addComment: (postId: number, text: string) =>
    api.post<ApiResponse<Comment>>(`/api/posts/${postId}/comments`, { text }),

  getComments: (postId: number, page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Comment>>>(`/api/posts/${postId}/comments`, { params: { page, size } }),

  deleteComment: (commentId: number) =>
    api.delete<ApiResponse<void>>(`/api/posts/comments/${commentId}`),
};
