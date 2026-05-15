-- ==========================================
-- Instagram Clone - Database DDL Script
-- ==========================================

-- ==========================================
-- Authentication Service Database
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_auth;
USE instagram_auth;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    bio VARCHAR(500),
    profile_picture VARCHAR(500),
    password_reset_token VARCHAR(255),
    password_reset_token_expiry DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_reset_token (password_reset_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Post Service Database
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_posts;
USE instagram_posts;

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(255),
    caption TEXT,
    media_url VARCHAR(1000),
    media_type ENUM('IMAGE', 'VIDEO', 'TEXT') NOT NULL,
    privacy ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    filter VARCHAR(100),
    likes_count BIGINT NOT NULL DEFAULT 0,
    views_count BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_privacy (privacy),
    INDEX idx_created_at (created_at),
    INDEX idx_likes_count (likes_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_hashtags (
    post_id BIGINT NOT NULL,
    hashtag VARCHAR(255) NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_hashtag (hashtag),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Post Service - Media Files Table (BLOB storage)
-- ==========================================
CREATE TABLE IF NOT EXISTS media_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_type VARCHAR(100) NOT NULL,
    filename VARCHAR(500),
    data MEDIUMBLOB NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Post Service - Comments Table
-- ==========================================
CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(255),
    text VARCHAR(2000) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Post Service - Video Views Table
-- ==========================================
CREATE TABLE IF NOT EXISTS video_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    watched_duration INT DEFAULT 0,
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Follow Service Database
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_follows;
USE instagram_follows;

CREATE TABLE IF NOT EXISTS follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    follower_username VARCHAR(255),
    following_id BIGINT NOT NULL,
    following_username VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follower_following (follower_id, following_id),
    INDEX idx_follower_id (follower_id),
    INDEX idx_following_id (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Follow Service - Notifications Table
-- ==========================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    sender_username VARCHAR(255),
    receiver_id BIGINT NOT NULL,
    type ENUM('LIKE', 'FOLLOW', 'NEW_POST', 'COMMENT', 'PASSWORD_RESET') NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Trending Service Database
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_trending;
USE instagram_trending;

CREATE TABLE IF NOT EXISTS trending_hashtags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hashtag VARCHAR(255) NOT NULL UNIQUE,
    post_count BIGINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hashtag (hashtag),
    INDEX idx_post_count (post_count),
    INDEX idx_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Sample Data
-- ==========================================
USE instagram_auth;
-- Note: Passwords are BCrypt hashed. Default password: Test@1234
INSERT INTO users (full_name, email, username, password, bio) VALUES
    ('John Doe', 'john@example.com', 'johndoe', '$2a$10$iL2gXkoS0B42ESpOyQEkFuak4mU.9yMrk7Hu1VS5sa1qhhv4Rxewm', 'Photography enthusiast'),
    ('Jane Smith', 'jane@example.com', 'janesmith', '$2a$10$iL2gXkoS0B42ESpOyQEkFuak4mU.9yMrk7Hu1VS5sa1qhhv4Rxewm', 'Travel blogger');

USE instagram_trending;
INSERT INTO trending_hashtags (hashtag, post_count, view_count) VALUES
    ('photography', 1500, 50000),
    ('travel', 1200, 45000),
    ('food', 1100, 40000),
    ('nature', 900, 35000),
    ('fitness', 800, 30000);
