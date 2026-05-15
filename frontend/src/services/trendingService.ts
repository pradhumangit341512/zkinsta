import api from './api';
import { ApiResponse, TrendingHashtag } from '../types';

export const trendingService = {
  getTrendingHashtags: (limit = 20) =>
    api.get<ApiResponse<TrendingHashtag[]>>('/api/trending/hashtags', { params: { limit } }),

  searchHashtags: (query: string) =>
    api.get<ApiResponse<TrendingHashtag[]>>('/api/trending/hashtags/search', { params: { query } }),
};
