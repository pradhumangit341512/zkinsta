export interface User {
  id: number;
  fullName: string;
  email: string;
  username: string;
  bio: string;
  profilePicture: string;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  fullName: string;
  message: string;
}

export interface Post {
  id: number;
  userId: number;
  username: string;
  caption: string;
  mediaUrl: string;
  mediaType: 'IMAGE' | 'VIDEO' | 'TEXT';
  privacy: 'PUBLIC' | 'FRIENDS_ONLY' | 'PRIVATE';
  filter: string;
  hashtags: string[];
  likesCount: number;
  viewsCount: number;
  commentsCount: number;
  likedByCurrentUser: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface FollowDto {
  id: number;
  followerId: number;
  followerUsername: string;
  followingId: number;
  followingUsername: string;
  createdAt: string;
}

export interface FollowCount {
  userId: number;
  followersCount: number;
  followingCount: number;
}

export interface TrendingHashtag {
  id: number;
  hashtag: string;
  postCount: number;
  viewCount: number;
  lastUpdated: string;
}

export interface ApiResponse<T = unknown> {
  message: string;
  success: boolean;
  timestamp: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  username: string;
  password: string;
  confirmPassword: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface CreatePostRequest {
  caption: string;
  mediaUrl: string;
  mediaType: 'IMAGE' | 'VIDEO' | 'TEXT';
  privacy: 'PUBLIC' | 'FRIENDS_ONLY' | 'PRIVATE';
  filter?: string;
  hashtags: string[];
}

export interface Comment {
  id: number;
  postId: number;
  userId: number;
  username: string;
  text: string;
  createdAt: string;
}

export interface Notification {
  id: number;
  senderId: number;
  senderUsername: string;
  receiverId: number;
  type: 'LIKE' | 'FOLLOW' | 'NEW_POST' | 'COMMENT' | 'PASSWORD_RESET';
  message: string;
  referenceId: number;
  read: boolean;
  createdAt: string;
}
