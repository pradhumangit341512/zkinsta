# ZKinsta - Instagram Clone Microservices Project Report

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [System Architecture](#3-system-architecture)
4. [Microservices Detailed Description](#4-microservices-detailed-description)
5. [Database Design](#5-database-design)
6. [Authentication Flow](#6-authentication-flow)
7. [API Gateway and Routing](#7-api-gateway-and-routing)
8. [Inter-Service Communication](#8-inter-service-communication)
9. [Frontend Architecture](#9-frontend-architecture)
10. [Frontend-Backend Integration](#10-frontend-backend-integration)
11. [Notification System](#11-notification-system)
12. [Service Discovery and Health Checks](#12-service-discovery-and-health-checks)
13. [Resilience Patterns](#13-resilience-patterns)
14. [Docker and Deployment](#14-docker-and-deployment)
15. [API Endpoints Reference](#15-api-endpoints-reference)
16. [How to Run the Project](#16-how-to-run-the-project)
17. [Project Flow Walkthroughs](#17-project-flow-walkthroughs)
18. [Viva Questions and Answers](#18-viva-questions-and-answers)

---

## 1. Project Overview

ZKinsta is a full-stack Instagram-like social media application built using a microservices architecture. The backend consists of five independent Spring Boot services that communicate through an API Gateway and a service registry. The frontend is a React single-page application that communicates with the backend through the API Gateway.

### Key Features
- User registration and login with JWT authentication
- Password reset via token-based flow
- Create, edit, delete posts with image/video upload
- Like and unlike posts
- Comment on posts
- Follow and unfollow users
- User profile pages with post grid
- Real-time notification system (follow, like, comment)
- Trending hashtags tracking
- Search for users, posts, and hashtags
- Post privacy settings (Public, Friends Only, Private)
- Image filters (12 Instagram-style CSS filters)
- View count tracking per post
- Responsive design (mobile, tablet, desktop)

---

## 2. Technology Stack

### Backend
| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.2.4 |
| Language | Java | 21 |
| Cloud | Spring Cloud | 2023.0.0 |
| API Gateway | Spring Cloud Gateway | - |
| Service Discovery | HashiCorp Consul | Latest |
| Database | MySQL | 8.0 |
| ORM | Spring Data JPA / Hibernate | - |
| Security | Spring Security + JWT (JJWT) | 0.12.5 |
| Resilience | Resilience4j Circuit Breaker | - |
| HTTP Client | Spring WebFlux WebClient | - |
| Load Balancing | Spring Cloud LoadBalancer | - |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.3.0 |
| Object Mapping | ModelMapper | 3.2.0 |
| Build Tool | Maven | - |
| Monitoring | Spring Boot Actuator | - |

### Frontend
| Component | Technology | Version |
|-----------|-----------|---------|
| Library | React | 18.3.1 |
| Language | TypeScript | 4.9.5 |
| Routing | React Router DOM | 6.28.0 |
| HTTP Client | Axios | 1.7.7 |
| Build Tool | Create React App | 5.0.1 |

### DevOps
| Component | Technology |
|-----------|-----------|
| Containerization | Docker + Docker Compose |
| Service Registry | HashiCorp Consul |

---

## 3. System Architecture

### High-Level Architecture Diagram (Text Representation)

```
                          +-------------------+
                          |    Frontend        |
                          |   React (Port 3000)|
                          +--------+----------+
                                   |
                                   | HTTP Requests
                                   v
                          +-------------------+
                          |   API Gateway     |
                          | (Port 8080)       |
                          | JWT Validation    |
                          | Route Management  |
                          | CORS Handling     |
                          | Load Balancing    |
                          +--------+----------+
                                   |
                    +--------------+--------------+
                    |              |              |
            +-------v------+ +----v-------+ +---v---------+
            | Auth Service | | Post       | | Follow      |
            | (Port 8081)  | | Service    | | Service     |
            | - Register   | | (Port 8082)| | (Port 8083) |
            | - Login      | | - CRUD Post| | - Follow    |
            | - Profile    | | - Like     | | - Unfollow  |
            | - JWT Token  | | - Comment  | | - Notify    |
            | - Search     | | - Media    | | - Followers |
            +-------+------+ +----+-------+ +---+---------+
                    |              |              |
                    |         +----v-------+     |
                    |         | Trending   |     |
                    |         | Service    |     |
                    |         | (Port 8084)|     |
                    |         | - Hashtags |     |
                    |         +----+-------+     |
                    |              |              |
            +-------v--------------v--------------v------+
            |              Consul Service Registry       |
            |              (Port 8500)                   |
            +--------------------------------------------+
                    |              |              |
            +-------v--------------v--------------v------+
            |              MySQL Database (Port 3306)    |
            |  instagram_auth | instagram_posts |        |
            |  instagram_follows | instagram_trending    |
            +--------------------------------------------+
```

### Architecture Pattern: Microservices

The application follows a microservices architecture where each service is:
- **Independently deployable** - Each service has its own Dockerfile and can be deployed separately
- **Independently scalable** - Each service can scale horizontally
- **Single responsibility** - Each service handles one domain (auth, posts, follows, trending)
- **Database per service** - Each service has its own database schema, ensuring loose coupling
- **Communicates via HTTP** - Services communicate using REST APIs through WebClient

---

## 4. Microservices Detailed Description

### 4.1 Authentication Service (Port 8081)

**Responsibility**: Manages user registration, login, profile management, password reset, and user search.

**Package**: `com.instagram.auth`

**Key Components**:
- `AuthController` - REST controller with all auth endpoints
- `AuthService` - Business logic with circuit breaker pattern
- `JwtUtil` - JWT token generation and validation
- `SecurityConfig` - Spring Security configuration
- `CustomUserDetailsService` - Loads user from database for Spring Security
- `User` entity - JPA entity mapped to `users` table

**How It Works**:
1. User sends registration data (fullName, email, username, password)
2. AuthService checks if email/username already exists
3. Password is encrypted using BCryptPasswordEncoder
4. User is saved to `instagram_auth.users` table
5. JWT token is generated containing username and userId
6. Token is returned to the frontend

**Password Storage**: Uses BCrypt hashing. The raw password is never stored. BCrypt automatically handles salting.

**JWT Token Contents**:
- Subject: username
- Claim "userId": the user's database ID
- Issued at: current timestamp
- Expiration: current timestamp + 24 hours (86400000 ms)
- Signed with: HMAC-SHA key derived from the secret string

---

### 4.2 Post Service (Port 8082)

**Responsibility**: Manages posts (CRUD), likes, comments, views, media uploads, and feed generation.

**Package**: `com.instagram.post`

**Key Components**:
- `PostController` - REST controller with all post endpoints
- `PostService` - Business logic for posts, likes, comments, views
- `Post` entity - JPA entity with mediaType enum (IMAGE, VIDEO, TEXT) and privacy enum (PUBLIC, FRIENDS_ONLY, PRIVATE)
- `Like` entity - Tracks which users liked which posts
- `Comment` entity - Stores comments on posts
- `VideoView` entity - Tracks post views with 5-minute cooldown
- `MediaFile` entity - Stores uploaded media metadata

**How It Works**:
1. When a user creates a post, the frontend first uploads the media file
2. The media file URL is returned
3. Frontend sends the post data (caption, mediaUrl, mediaType, privacy, hashtags, filter)
4. PostService saves the post and calls trending-service to update hashtag counts
5. When a user likes a post, a LIKE notification is sent to the post owner via follow-service
6. When a user comments, a COMMENT notification is sent to the post owner

**View Tracking**: When a post scrolls into the viewport (detected by IntersectionObserver on frontend), a view is recorded. A 5-minute cooldown prevents duplicate views from the same user.

**Username Resolution**: Since the Post database only stores userId (not username), the service uses WebClient to call authentication-service to fetch usernames when returning post data.

---

### 4.3 Follow Service (Port 8083)

**Responsibility**: Manages follow/unfollow relationships and the notification system.

**Package**: `com.instagram.follow`

**Key Components**:
- `FollowController` - REST controller for follow operations
- `FollowService` - Business logic for follow/unfollow
- `NotificationController` - REST controller for notification management
- `NotificationService` - Business logic for creating and managing notifications
- `Follow` entity - JPA entity with unique constraint on (follower_id, following_id)
- `Notification` entity - JPA entity with NotificationType enum (LIKE, FOLLOW, NEW_POST, COMMENT, PASSWORD_RESET)

**How It Works**:
1. User A follows User B by sending a POST request with User B's ID
2. FollowService validates that User A is not following themselves
3. FollowService checks if the follow relationship already exists
4. Follow record is saved to database
5. A FOLLOW notification is created for User B
6. Username is fetched from authentication-service via WebClient

**Notification System**: The notification system lives in the follow-service because notifications are closely related to social interactions. Other services (like post-service) create notifications by calling the follow-service's notification endpoint via WebClient.

---

### 4.4 Trending Service (Port 8084)

**Responsibility**: Tracks trending hashtags by counting how many posts use each hashtag and how many views those hashtags receive.

**Package**: `com.instagram.trending`

**Key Components**:
- `TrendingController` - REST controller for trending operations
- `TrendingService` - Business logic with circuit breaker
- `TrendingHashtag` entity - JPA entity tracking hashtag popularity

**How It Works**:
1. When a post is created with hashtags, the post-service calls the trending-service for each hashtag
2. TrendingService creates the hashtag if it does not exist, or increments its postCount
3. When trending posts are viewed, viewCount is incremented
4. Trending hashtags are returned sorted by postCount in descending order
5. Frontend displays top trending hashtags in sidebar and trending page

---

### 4.5 API Gateway (Port 8080)

**Responsibility**: Acts as the single entry point for all client requests. Handles routing, JWT validation, CORS, and load balancing.

**Package**: `com.instagram.gateway`

**Key Components**:
- `ApiGatewayApplication` - Entry point with @EnableDiscoveryClient
- `GatewayConfig` - Configuration class
- `JwtAuthenticationFilter` - Custom gateway filter for JWT validation

**How It Works**:
1. Frontend sends all requests to port 8080
2. API Gateway matches the request path against configured routes
3. For protected routes, JwtAuthenticationFilter validates the JWT token
4. If valid, the filter extracts username and userId from the token
5. These values are added as headers (X-Username, X-User-Id) to the downstream request
6. The request is forwarded to the appropriate service using load-balanced URIs (lb://service-name)
7. Consul is used for service discovery to resolve service names to actual IP:port

**Route Types**:
- **Public routes** - No JWT required (register, login, public feed, trending)
- **Protected routes** - JWT required (create post, like, comment, follow, notifications)

---

## 5. Database Design

### 5.1 Entity Relationship Overview

The project uses four separate MySQL databases following the database-per-service pattern:

#### Database: instagram_auth

```
+---------------------------+
|         users             |
+---------------------------+
| id (PK, BIGINT)          |
| full_name (VARCHAR 255)   |
| email (VARCHAR 255, UQ)   |
| username (VARCHAR 255, UQ)|
| password (VARCHAR 255)    |
| bio (VARCHAR 500)         |
| profile_picture (VARCHAR) |
| password_reset_token      |
| password_reset_token_expiry|
| created_at (DATETIME)     |
| updated_at (DATETIME)     |
+---------------------------+
```

#### Database: instagram_posts

```
+---------------------------+     +---------------------------+
|         posts             |     |      post_hashtags        |
+---------------------------+     +---------------------------+
| id (PK, BIGINT)          |<----| post_id (FK)              |
| user_id (BIGINT)         |     | hashtag (VARCHAR)         |
| caption (VARCHAR 2000)    |     +---------------------------+
| media_url (VARCHAR)       |
| media_type (ENUM)         |     +---------------------------+
| privacy (ENUM)            |     |      post_likes           |
| filter (VARCHAR)          |     +---------------------------+
| likes_count (BIGINT)      |<----| id (PK)                  |
| views_count (BIGINT)      |     | post_id (FK)             |
| created_at (DATETIME)     |     | user_id (BIGINT)         |
| updated_at (DATETIME)     |     | created_at (DATETIME)    |
+---------------------------+     +---------------------------+
                                  | UQ: (post_id, user_id)   |
                                  +---------------------------+

+---------------------------+     +---------------------------+
|     post_comments         |     |      video_views          |
+---------------------------+     +---------------------------+
| id (PK, BIGINT)          |     | id (PK, BIGINT)          |
| post_id (FK)             |     | post_id (FK)             |
| user_id (BIGINT)         |     | user_id (BIGINT)         |
| text (VARCHAR 2000)       |     | watched_duration (INT)   |
| created_at (DATETIME)     |     | viewed_at (DATETIME)     |
+---------------------------+     +---------------------------+
```

#### Database: instagram_follows

```
+---------------------------+     +---------------------------+
|        follows            |     |     notifications         |
+---------------------------+     +---------------------------+
| id (PK, BIGINT)          |     | id (PK, BIGINT)          |
| follower_id (BIGINT)     |     | sender_id (BIGINT)       |
| following_id (BIGINT)    |     | receiver_id (BIGINT)     |
| created_at (DATETIME)     |     | type (ENUM)              |
+---------------------------+     | message (VARCHAR 500)    |
| UQ: (follower_id,        |     | reference_id (BIGINT)    |
|      following_id)        |     | is_read (BOOLEAN)        |
+---------------------------+     | created_at (DATETIME)     |
                                  +---------------------------+
```

#### Database: instagram_trending

```
+---------------------------+
|   trending_hashtags       |
+---------------------------+
| id (PK, BIGINT)          |
| hashtag (VARCHAR 255, UQ) |
| post_count (BIGINT)       |
| view_count (BIGINT)       |
| last_updated (DATETIME)   |
+---------------------------+
```

### 5.2 Why Database Per Service?

Each microservice owns its data. This means:
- Services are loosely coupled - changing one database schema does not affect others
- Services can be deployed independently
- Each service can choose the best database type for its needs (all use MySQL here, but could differ)
- Data consistency is handled at the application level, not database level
- Services query other services' data through API calls, not direct database joins

---

## 6. Authentication Flow

### 6.1 Registration Flow

```
Frontend                   API Gateway              Auth Service            MySQL
   |                           |                         |                    |
   |  POST /api/auth/register  |                         |                    |
   |  {fullName, email,        |                         |                    |
   |   username, password}     |                         |                    |
   |-------------------------->|  (Public route - no JWT) |                    |
   |                           |------------------------>|                    |
   |                           |                         | Check email exists  |
   |                           |                         |------------------->|
   |                           |                         |<-------------------|
   |                           |                         | Check username exists|
   |                           |                         |------------------->|
   |                           |                         |<-------------------|
   |                           |                         | BCrypt(password)    |
   |                           |                         | Save User           |
   |                           |                         |------------------->|
   |                           |                         |<-------------------|
   |                           |                         | Generate JWT token  |
   |                           |<------------------------|                    |
   |  {token, username, msg}   |                         |                    |
   |<--------------------------|                         |                    |
   |                           |                         |                    |
   |  Store token in           |                         |                    |
   |  localStorage             |                         |                    |
```

### 6.2 Login Flow

```
Frontend                   API Gateway              Auth Service            MySQL
   |                           |                         |                    |
   |  POST /api/auth/login     |                         |                    |
   |  {username, password}     |                         |                    |
   |-------------------------->|  (Public route - no JWT) |                    |
   |                           |------------------------>|                    |
   |                           |                         | Find user by       |
   |                           |                         | username            |
   |                           |                         |------------------->|
   |                           |                         |<-------------------|
   |                           |                         | BCrypt.matches(     |
   |                           |                         |   password,         |
   |                           |                         |   user.password)    |
   |                           |                         | Generate JWT token  |
   |                           |<------------------------|                    |
   |  {token, username, msg}   |                         |                    |
   |<--------------------------|                         |                    |
   |                           |                         |                    |
   |  Store token in           |                         |                    |
   |  localStorage             |                         |                    |
   |  Fetch profile            |                         |                    |
   |  Redirect to home         |                         |                    |
```

### 6.3 Authenticated Request Flow

```
Frontend                   API Gateway              Any Service
   |                           |                         |
   |  GET /api/posts/public    |                         |
   |  Header: Authorization:   |                         |
   |    Bearer <JWT_TOKEN>     |                         |
   |-------------------------->|                         |
   |                           | JwtAuthenticationFilter |
   |                           | 1. Extract token from   |
   |                           |    "Bearer " prefix     |
   |                           | 2. Parse JWT claims     |
   |                           | 3. Extract username     |
   |                           | 4. Extract userId       |
   |                           | 5. Add X-Username header|
   |                           | 6. Add X-User-Id header |
   |                           |------------------------>|
   |                           |                         | Read X-Username
   |                           |                         | Read X-User-Id
   |                           |                         | Process request
   |                           |<------------------------|
   |  Response                 |                         |
   |<--------------------------|                         |
```

### 6.4 Password Reset Flow

```
Step 1: Request Reset
Frontend -> POST /api/auth/forgot-password {email}
         -> AuthService generates UUID reset token
         -> Stores token + 1-hour expiry in database
         -> Returns token to user (in production, this would be emailed)

Step 2: Reset Password
Frontend -> POST /api/auth/reset-password {token, newPassword, confirmPassword}
         -> AuthService validates token exists and is not expired
         -> BCrypt encodes new password
         -> Updates password in database
         -> Clears reset token and expiry
```

### 6.5 JWT Token Structure

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "john_doe",           // username
  "userId": 1,                 // database user ID
  "iat": 1700000000,           // issued at (Unix timestamp)
  "exp": 1700086400            // expires (24 hours later)
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

The secret key is: `MySecretKeyForJWTTokenGeneration2024InstagramCloneApp`

---

## 7. API Gateway and Routing

### 7.1 Route Configuration

The API Gateway uses Spring Cloud Gateway to route requests. Routes are defined in `application.yml`:

| Route ID | Path Pattern | Target Service | Auth Required |
|----------|-------------|----------------|---------------|
| auth-public | /api/auth/register, /api/auth/login, /api/auth/forgot-password, /api/auth/reset-password, /api/auth/users/**, /api/auth/check-** | authentication-service | No |
| auth-profile-public | /api/auth/profile/{username} (GET only) | authentication-service | No |
| auth-protected | /api/auth/** | authentication-service | Yes |
| media-serve | /api/media/{id} (GET only) | post-service | No |
| media-upload | /api/media/** | post-service | Yes |
| post-comments-public | /api/posts/{postId}/comments (GET only) | post-service | No |
| post-public | /api/posts/public, /api/posts/trending, /api/posts/trending/recent, /api/posts/search, /api/posts/hashtag/** | post-service | No |
| post-service | /api/posts/** | post-service | Yes |
| follow-service | /api/follows/** | follow-service | Yes |
| notification-service | /api/notifications/** | follow-service | Yes |
| trending-service | /api/trending/** | trending-service | No |

### 7.2 CORS Configuration

```yaml
Allowed Origins: http://localhost:3000, :3001, :3002
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: * (all)
Allow Credentials: true
Max Age: 3600 seconds
```

### 7.3 How Load Balancing Works

The gateway uses `lb://service-name` URI scheme:
1. When a request comes in, the gateway resolves `service-name` using Consul
2. Consul returns all registered instances of that service
3. Spring Cloud LoadBalancer picks one instance (round-robin by default)
4. The request is forwarded to that instance

---

## 8. Inter-Service Communication

### 8.1 WebClient (Synchronous HTTP Calls)

Services communicate with each other using Spring WebFlux's WebClient with `@LoadBalanced` annotation for Consul-based service discovery.

**Communication Patterns**:

```
Post Service --> Authentication Service
  Purpose: Fetch usernames for posts (posts only store userId)
  Call: GET http://authentication-service/api/auth/users/{userId}

Post Service --> Trending Service
  Purpose: Update hashtag counts when a post is created
  Call: POST http://trending-service/api/trending/hashtags/{hashtag}

Post Service --> Follow Service
  Purpose: Send LIKE and COMMENT notifications
  Call: POST http://follow-service/api/notifications

Follow Service --> Authentication Service
  Purpose: Fetch usernames for follow relationships and notifications
  Call: GET http://authentication-service/api/auth/users/{userId}
```

### 8.2 How WebClient Works in This Project

```java
// Example: PostService fetching a username
Map<String, Object> response = webClientBuilder.build()
    .get()
    .uri("http://authentication-service/api/auth/users/{userId}", userId)
    .retrieve()
    .bodyToMono(Map.class)
    .block();
```

Step-by-step:
1. `webClientBuilder.build()` - Creates a WebClient instance with @LoadBalanced support
2. `.get()` - Sets HTTP method to GET
3. `.uri(...)` - Sets the target URI. `authentication-service` is resolved by Consul
4. `.retrieve()` - Sends the request
5. `.bodyToMono(Map.class)` - Reads the response body as a Map
6. `.block()` - Blocks until the response is received (synchronous call)

### 8.3 Why WebClient Instead of RestTemplate?

- RestTemplate is deprecated in newer Spring versions
- WebClient supports both synchronous (.block()) and asynchronous (reactive) calls
- WebClient integrates with Spring Cloud LoadBalancer for service discovery
- WebClient has better connection management and is more efficient

---

## 9. Frontend Architecture

### 9.1 Project Structure

```
frontend/src/
  |-- App.tsx                  # Root component with routing
  |-- App.css                  # Global layout styles
  |-- index.tsx                # Entry point
  |-- index.css                # Reset and base styles
  |-- context/
  |   |-- AuthContext.tsx       # Authentication state management
  |-- types/
  |   |-- index.ts             # TypeScript interfaces
  |-- services/
  |   |-- api.ts               # Axios instance with interceptors
  |   |-- authService.ts       # Auth API calls
  |   |-- postService.ts       # Post API calls
  |   |-- followService.ts     # Follow API calls
  |   |-- notificationService.ts # Notification API calls
  |   |-- trendingService.ts   # Trending API calls
  |-- components/
      |-- auth/
      |   |-- Login.tsx         # Login page
      |   |-- Register.tsx      # Registration page
      |   |-- ForgotPassword.tsx # Password reset page
      |   |-- Auth.css          # Auth styles
      |-- layout/
      |   |-- Navbar.tsx        # Sidebar navigation + notifications
      |   |-- Navbar.css        # Navigation styles
      |-- posts/
      |   |-- HomeFeed.tsx      # Home feed with infinite loading
      |   |-- PostCard.tsx      # Individual post component
      |   |-- CreatePost.tsx    # Post creation form
      |   |-- Posts.css         # Post styles
      |-- profile/
      |   |-- Profile.tsx       # User profile page
      |   |-- Profile.css       # Profile styles
      |-- search/
      |   |-- Search.tsx        # Search page
      |   |-- Search.css        # Search styles
      |-- trending/
      |   |-- Trending.tsx      # Trending page
      |   |-- Trending.css      # Trending styles
      |-- media/
          |-- MediaEditor.tsx   # Image filter editor
          |-- MediaEditor.css   # Editor styles
          |-- cropUtils.ts      # Canvas-based image processing
```

### 9.2 State Management

The application uses React Context API for global state (authentication) and local component state (useState) for component-specific state.

**AuthContext** provides:
- `user` - Current logged-in user object
- `token` - JWT token string
- `isAuthenticated` - Boolean flag
- `login(token)` - Stores token, fetches profile
- `logout()` - Clears token and user
- `refreshProfile()` - Re-fetches user profile

**No Redux or external state management library is used** - React Context is sufficient for this application's needs.

### 9.3 Routing

| Path | Component | Auth Required | Description |
|------|-----------|---------------|-------------|
| / | HomeFeed | Yes | Home feed with posts |
| /login | Login | No | Login page |
| /register | Register | No | Registration page |
| /forgot-password | ForgotPassword | No | Password reset |
| /create-post | CreatePost | Yes | Post creation form |
| /profile/:username | Profile | No | User profile page |
| /search | Search | No | Search page |
| /trending | Trending | No | Trending page |

Protected routes use the `ProtectedRoute` component that checks `isAuthenticated` and redirects to `/login` if false.

### 9.4 Responsive Design

Three breakpoints are used:

| Breakpoint | Screen | Sidebar | Main Content |
|------------|--------|---------|-------------|
| < 768px | Mobile | Hidden (bottom bar shows) | Full width, 50px bottom margin |
| 768px - 1263px | Tablet | 72px collapsed (icons only) | margin-left: 72px |
| >= 1264px | Desktop | 245px full (icons + text) | margin-left: 245px |

---

## 10. Frontend-Backend Integration

### 10.1 API Client Setup

The frontend uses Axios as the HTTP client. A centralized `api.ts` file creates an Axios instance:

```
Base URL: http://localhost:8080 (API Gateway)
Timeout: 10000ms
Content-Type: application/json
```

**Request Interceptor**: Automatically attaches the JWT token from localStorage to every request:
```
Authorization: Bearer <token>
```

**Response Interceptor**: If any API call returns 401 (Unauthorized), the token is removed from localStorage and the user is redirected to the login page.

### 10.2 API Response Format

All backend responses follow a consistent format:

```json
{
  "message": "Success message",
  "success": true,
  "timestamp": "2024-01-01T12:00:00",
  "data": { ... }
}
```

For paginated responses, the `data` field contains:
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

### 10.3 Service Layer Mapping

| Frontend Service | Backend Service | Base Path |
|-----------------|----------------|-----------|
| authService.ts | authentication-service | /api/auth |
| postService.ts | post-service | /api/posts |
| followService.ts | follow-service | /api/follows |
| notificationService.ts | follow-service | /api/notifications |
| trendingService.ts | trending-service | /api/trending |

---

## 11. Notification System

### 11.1 Notification Types

| Type | Trigger | Message Format | Example |
|------|---------|---------------|---------|
| FOLLOW | User A follows User B | "{username} started following you" | john started following you |
| LIKE | User A likes User B's post | "{username} liked your post" | john liked your post |
| COMMENT | User A comments on User B's post | "{username} commented on your post: {preview}" | john commented on your post: Nice photo! |

### 11.2 Notification Flow

```
1. FOLLOW Notification (created in follow-service):
   User clicks Follow -> FollowService.followUser()
   -> NotificationService.createNotification(type=FOLLOW)
   -> Saved to notifications table

2. LIKE Notification (created via inter-service call):
   User clicks Like -> PostService.likePost()
   -> PostService.sendNotification() via WebClient
   -> POST http://follow-service/api/notifications
   -> NotificationService.createNotification(type=LIKE)
   -> Saved to notifications table

3. COMMENT Notification (created via inter-service call):
   User posts comment -> PostService.addComment()
   -> PostService.sendNotification() via WebClient
   -> POST http://follow-service/api/notifications
   -> NotificationService.createNotification(type=COMMENT)
   -> Saved to notifications table
```

### 11.3 Frontend Notification Polling

The Navbar component polls for notifications every 30 seconds:
1. Calls `GET /api/notifications?page=0&size=10` to get recent notifications
2. Calls `GET /api/notifications/unread-count` to get the unread badge count
3. Displays notification panel with type-specific icons (heart, comment, person)
4. Clicking a notification marks it as read and navigates to sender's profile

---

## 12. Service Discovery and Health Checks

### 12.1 How Consul Works

1. Each service registers itself with Consul on startup using Spring Cloud Consul Discovery
2. The registration includes: service name, instance ID, IP address, port
3. Consul periodically checks the health of each service via HTTP health checks
4. The API Gateway queries Consul to discover available service instances
5. If a service goes down, Consul removes it from the registry

### 12.2 Health Check Configuration

Each service exposes a health endpoint via Spring Boot Actuator:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

Consul sends periodic HTTP GET requests to `/actuator/health` on each service. If the response is 200 OK with status "UP", the service is marked healthy (green). Otherwise, it is marked unhealthy (red).

### 12.3 Service Registration Properties

```yaml
spring.cloud.consul:
  host: localhost
  port: 8500
  discovery:
    service-name: <service-name>
    instance-id: ${spring.application.name}:${random.value}
    prefer-ip-address: true
```

The `instance-id` uses a random value to ensure unique IDs when multiple instances of the same service run.

---

## 13. Resilience Patterns

### 13.1 Circuit Breaker (Resilience4j)

Each service uses the Circuit Breaker pattern to prevent cascading failures:

```yaml
resilience4j.circuitbreaker.instances.<name>:
  register-health-indicator: true
  sliding-window-size: 10
  minimum-number-of-calls: 5
  failure-rate-threshold: 50
  wait-duration-in-open-state: 60s
  permitted-number-of-calls-in-half-open-state: 3
  sliding-window-type: COUNT_BASED
  slow-call-duration-threshold: 3s
```

**How It Works**:

```
CLOSED State (Normal):
  - All requests pass through
  - Failures are counted in a sliding window of 10 calls
  - If failure rate exceeds 50% (after minimum 5 calls), circuit OPENS

OPEN State (Failure):
  - All requests are immediately rejected
  - Fallback method is called instead
  - After 60 seconds, circuit moves to HALF-OPEN

HALF-OPEN State (Testing):
  - 3 requests are allowed through
  - If they succeed, circuit CLOSES
  - If they fail, circuit OPENS again
```

### 13.2 Fallback Methods

When the circuit is open, fallback methods are called:

```java
@CircuitBreaker(name = "postService", fallbackMethod = "createPostFallback")
public PostDto createPost(...) { ... }

public PostDto createPostFallback(..., Throwable t) {
    if (t instanceof CustomException) throw (CustomException) t;
    throw new CustomException("Service temporarily unavailable", 503);
}
```

Fallbacks re-throw business exceptions (like "Post not found") but convert infrastructure failures to a user-friendly message.

---

## 14. Docker and Deployment

### 14.1 Docker Compose Architecture

The `docker-compose.yml` defines 8 services:

| Service | Image | Port | Depends On |
|---------|-------|------|-----------|
| mysql | mysql:8.0 | 3306 | - |
| consul | hashicorp/consul:latest | 8500 | - |
| authentication-service | Custom Dockerfile | 8081 | mysql (healthy), consul |
| post-service | Custom Dockerfile | 8082 | mysql (healthy), consul |
| follow-service | Custom Dockerfile | 8083 | mysql (healthy), consul |
| trending-service | Custom Dockerfile | 8084 | mysql (healthy), consul |
| api-gateway | Custom Dockerfile | 8080 | All services |
| frontend | Custom Dockerfile | 3000 | api-gateway |

### 14.2 Startup Order

1. MySQL starts first with a health check (mysqladmin ping)
2. Consul starts in parallel
3. Backend services wait for MySQL to be healthy AND Consul to start
4. API Gateway waits for all backend services
5. Frontend waits for API Gateway

### 14.3 Environment Variables (Docker)

Services receive database URLs and Consul host via environment variables:
```
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/<db_name>
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: root
SPRING_CLOUD_CONSUL_HOST: consul
```

Note: In Docker, service names (`mysql`, `consul`) are used as hostnames instead of `localhost`.

---

## 15. API Endpoints Reference

### Authentication Service (Port 8081)

| Method | Endpoint | Auth | Description |
|--------|---------|------|-------------|
| POST | /api/auth/register | No | Register new user |
| POST | /api/auth/login | No | Login |
| GET | /api/auth/profile | Yes | Get current user profile |
| GET | /api/auth/profile/{username} | No | Get user profile by username |
| PUT | /api/auth/profile | Yes | Update profile |
| POST | /api/auth/forgot-password | No | Request password reset |
| POST | /api/auth/reset-password | No | Reset password with token |
| GET | /api/auth/users/search?query= | No | Search users |
| GET | /api/auth/users/{userId} | No | Get user by ID |
| GET | /api/auth/check-username/{username} | No | Check username availability |
| GET | /api/auth/check-email/{email} | No | Check email availability |

### Post Service (Port 8082)

| Method | Endpoint | Auth | Description |
|--------|---------|------|-------------|
| POST | /api/posts | Yes | Create post |
| GET | /api/posts/{postId} | Optional | Get post by ID |
| GET | /api/posts/user/{userId} | Optional | Get posts by user |
| GET | /api/posts/feed?followingIds=&page=&size= | Yes | Get following feed |
| GET | /api/posts/public?page=&size= | Optional | Get public feed |
| PUT | /api/posts/{postId} | Yes | Update post |
| DELETE | /api/posts/{postId} | Yes | Delete post |
| POST | /api/posts/{postId}/like | Yes | Like post |
| DELETE | /api/posts/{postId}/like | Yes | Unlike post |
| GET | /api/posts/trending?page=&size= | Optional | Get trending posts |
| GET | /api/posts/trending/recent?hours=&page=&size= | Optional | Get recent trending |
| GET | /api/posts/hashtag/{hashtag}?page=&size= | Optional | Get posts by hashtag |
| GET | /api/posts/search?query=&page=&size= | Optional | Search posts |
| POST | /api/posts/{postId}/view | Yes | Record view |
| POST | /api/posts/{postId}/comments | Yes | Add comment |
| GET | /api/posts/{postId}/comments?page=&size= | No | Get comments |
| DELETE | /api/posts/comments/{commentId} | Yes | Delete comment |

### Follow Service (Port 8083)

| Method | Endpoint | Auth | Description |
|--------|---------|------|-------------|
| POST | /api/follows | Yes | Follow user |
| DELETE | /api/follows/{followingId} | Yes | Unfollow user |
| GET | /api/follows/followers/{userId} | Yes | Get followers |
| GET | /api/follows/following/{userId} | Yes | Get following |
| GET | /api/follows/count/{userId} | Yes | Get follow counts |
| GET | /api/follows/check?followingId= | Yes | Check if following |
| GET | /api/follows/following-ids/{userId} | Yes | Get following IDs |

### Notification Endpoints (Port 8083 - Follow Service)

| Method | Endpoint | Auth | Description |
|--------|---------|------|-------------|
| POST | /api/notifications | Internal | Create notification |
| GET | /api/notifications?page=&size= | Yes | Get notifications |
| GET | /api/notifications/unread-count | Yes | Get unread count |
| PUT | /api/notifications/{id}/read | Yes | Mark as read |
| PUT | /api/notifications/read-all | Yes | Mark all as read |

### Trending Service (Port 8084)

| Method | Endpoint | Auth | Description |
|--------|---------|------|-------------|
| GET | /api/trending/hashtags?limit= | No | Get trending hashtags |
| POST | /api/trending/hashtags/{hashtag} | Internal | Update hashtag count |
| POST | /api/trending/hashtags/{hashtag}/view | No | Increment view count |
| GET | /api/trending/hashtags/search?query= | No | Search hashtags |

---

## 16. How to Run the Project

### Prerequisites
- Java 21 (JDK)
- Maven
- Node.js (v18 or later)
- MySQL 8.0
- HashiCorp Consul
- Docker and Docker Compose (for containerized deployment)

### Option 1: Run with Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/pradhumangit341512/zkinsta.git
cd zkinsta

# Start all services
docker-compose up --build

# Access the application
# Frontend: http://localhost:3000
# API Gateway: http://localhost:8080
# Consul UI: http://localhost:8500
# Swagger (Auth): http://localhost:8081/swagger-ui.html
# Swagger (Posts): http://localhost:8082/swagger-ui.html
# Swagger (Follows): http://localhost:8083/swagger-ui.html
# Swagger (Trending): http://localhost:8084/swagger-ui.html
```

### Option 2: Run Locally (Development)

```bash
# Step 1: Start MySQL
# Make sure MySQL is running on port 3306 with root/root credentials
# Run the DDL script: database/ddl-script.sql

# Step 2: Start Consul
consul agent -dev -ui

# Step 3: Start backend services (each in a separate terminal)
cd authentication-service && mvn spring-boot:run
cd post-service && mvn spring-boot:run
cd follow-service && mvn spring-boot:run
cd trending-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run

# Step 4: Start frontend
cd frontend && npm install && npm start

# Access: http://localhost:3000
```

### Verification Checklist
1. Open Consul UI at http://localhost:8500 - all services should be green
2. Open http://localhost:3000 - frontend should load
3. Register a new user - should get redirected to home
4. Create a post - should appear in feed
5. Search for users - should show results
6. Follow a user - notification should appear
7. Like a post - notification should appear for post owner

---

## 17. Project Flow Walkthroughs

### 17.1 Complete User Registration to First Post Flow

```
1. User opens http://localhost:3000 -> Redirected to /login (not authenticated)
2. User clicks "Sign up" -> Navigated to /register
3. User fills: Full Name, Email, Username, Password, Confirm Password
   - Real-time availability check for username and email
   - Client-side validation for password match
4. User clicks "Sign up" button
5. Frontend -> POST /api/auth/register (via API Gateway)
6. Auth Service creates user with BCrypt password -> Returns JWT token
7. Frontend stores token in localStorage
8. Frontend fetches profile via GET /api/auth/profile
9. User is redirected to home feed (/)
10. Home feed loads public posts via GET /api/posts/public
11. User clicks "Create" in sidebar -> Navigated to /create-post
12. User selects an image file (validated: <10MB, image type)
13. User optionally clicks "Edit Photo" -> MediaEditor opens
14. User selects a filter (e.g., Clarendon) and adjusts brightness
15. User clicks "Apply" -> Filter is applied via Canvas API
16. User writes caption, adds hashtags, selects privacy
17. User clicks "Share Post"
18. Frontend uploads image -> POST /api/media/upload -> Returns mediaUrl
19. Frontend creates post -> POST /api/posts (with mediaUrl, caption, hashtags)
20. Post Service saves post, calls Trending Service to update hashtag counts
21. User is redirected to home feed where the new post appears
```

### 17.2 Like and Notification Flow

```
1. User A scrolls through feed and sees User B's post
2. IntersectionObserver fires -> POST /api/posts/{id}/view (view recorded)
3. User A double-taps or clicks heart icon
4. Frontend optimistically updates UI (heart turns red, count +1)
5. Frontend -> POST /api/posts/{id}/like
6. Post Service increments likesCount
7. Post Service -> POST http://follow-service/api/notifications
   {senderId: A, receiverId: B, type: "LIKE", message: "userA liked your post"}
8. Notification saved in database
9. User B's Navbar polls notifications (every 30 seconds)
10. Red badge appears with unread count
11. User B clicks heart icon -> Notification panel opens
12. User B sees: [heart icon] "userA liked your post" - "2m"
13. User B clicks the notification -> Marked as read, navigated to userA's profile
```

### 17.3 Follow Flow

```
1. User A visits /profile/userB
2. Frontend loads profile: GET /api/auth/profile/userB
3. Frontend loads posts: GET /api/posts/user/{userBId}
4. Frontend loads follow counts: GET /api/follows/count/{userBId}
5. Frontend checks if following: GET /api/follows/check?followingId={userBId}
6. "Follow" button is displayed (not following yet)
7. User A clicks "Follow"
8. Frontend optimistically updates UI (button changes to "Following", follower count +1)
9. Frontend -> POST /api/follows {followingId: userBId}
10. Follow Service creates follow record
11. Follow Service creates FOLLOW notification for User B
12. User B receives notification: "userA started following you"
```

---

## 18. Viva Questions and Answers

### Architecture Questions

**Q1: What is microservices architecture and why did you use it?**

A: Microservices architecture is a software design pattern where the application is divided into small, independent services, each responsible for a specific business function. We used it because:
- Each service can be developed, deployed, and scaled independently
- Different teams can work on different services simultaneously
- If one service fails (e.g., trending), the rest of the application continues working
- Each service can have its own database, reducing coupling
- It makes the application more maintainable as it grows

**Q2: What is the role of the API Gateway in your project?**

A: The API Gateway (Spring Cloud Gateway, Port 8080) serves as the single entry point for all frontend requests. Its responsibilities are:
- Route requests to the correct microservice based on URL path
- Validate JWT tokens for protected endpoints
- Extract user information from JWT and pass it to downstream services via headers (X-User-Id, X-Username)
- Handle CORS (Cross-Origin Resource Sharing) configuration
- Load balance requests across multiple instances of a service
- Deduplicate CORS response headers

**Q3: How does service discovery work in your project?**

A: We use HashiCorp Consul for service discovery. Each microservice registers itself with Consul on startup, providing its name, IP address, and port. The API Gateway queries Consul to find available instances of each service. This means services do not need to know each other's exact addresses - they only need to know the service name (e.g., "authentication-service"). Consul also performs health checks by periodically calling each service's /actuator/health endpoint.

**Q4: What is a Circuit Breaker and why did you use Resilience4j?**

A: A Circuit Breaker is a design pattern that prevents cascading failures in distributed systems. We used Resilience4j because:
- If authentication-service goes down, all calls to it would timeout and slow down other services
- The circuit breaker monitors failure rates (sliding window of 10 calls)
- If more than 50% of calls fail, the circuit "opens" and immediately returns fallback responses
- After 60 seconds, it allows 3 test calls (half-open state)
- If those succeed, the circuit closes and normal operation resumes
- This prevents one failing service from taking down the entire system

**Q5: Why did you use database-per-service pattern instead of a shared database?**

A: The database-per-service pattern ensures:
- Loose coupling: changing the posts schema does not affect the auth service
- Independent deployment: each service can migrate its database independently
- Better performance: each database is optimized for its service's queries
- Clear ownership: each team owns their data
- Technology freedom: different services could use different databases (MySQL, MongoDB, etc.)
The tradeoff is that cross-service queries require API calls instead of SQL joins.

---

### Authentication Questions

**Q6: How does JWT authentication work in your project?**

A: JWT (JSON Web Token) authentication works in 3 steps:
1. **Login**: User sends username and password. The auth service validates credentials using BCrypt, then generates a JWT token containing the username and userId, signed with a secret key, with a 24-hour expiration.
2. **Storage**: The frontend stores the token in localStorage.
3. **Usage**: Every API request includes the token in the Authorization header ("Bearer <token>"). The API Gateway's JwtAuthenticationFilter validates the token signature and expiration, extracts the username and userId, and passes them as headers to downstream services.

**Q7: Why did you use BCrypt for password hashing?**

A: BCrypt is a password hashing function designed specifically for passwords:
- It automatically generates a random salt for each password, so identical passwords have different hashes
- It has a configurable work factor that can be increased over time as hardware gets faster
- It is deliberately slow to compute, making brute-force attacks impractical
- It is the industry standard recommended by OWASP for password storage
- Spring Security provides BCryptPasswordEncoder out of the box

**Q8: What happens when a JWT token expires?**

A: When a token expires (after 24 hours):
1. The API Gateway's JWT filter rejects the request with 401 Unauthorized
2. The frontend's Axios response interceptor catches the 401 response
3. It removes the token from localStorage
4. It redirects the user to the login page
5. The user must log in again to get a new token

**Q9: How does the password reset flow work?**

A: The password reset uses a token-based flow:
1. User provides their email address
2. Auth service generates a UUID token and stores it in the database with a 1-hour expiry
3. In production, this token would be emailed; in our demo, it is returned in the response
4. User provides the token along with the new password
5. Auth service validates the token (exists, not expired), then BCrypt-hashes the new password
6. The password is updated and the reset token is cleared

---

### Frontend Questions

**Q10: How does the frontend manage authentication state?**

A: We use React Context API (AuthContext) to manage authentication state globally:
- `AuthProvider` wraps the entire application
- On app load, it checks localStorage for an existing token
- If found, it fetches the user profile from the backend
- The `useAuth()` hook provides access to: user, token, isAuthenticated, login(), logout(), refreshProfile()
- `ProtectedRoute` component checks `isAuthenticated` and redirects to login if false
- When token is set via `login()`, it is stored in localStorage and the profile is fetched

**Q11: How does the frontend communicate with the backend?**

A: The frontend uses Axios (HTTP client) with a centralized configuration:
- Base URL points to the API Gateway (http://localhost:8080)
- A request interceptor automatically adds the JWT token to every request
- A response interceptor catches 401 errors and redirects to login
- Each service module (authService, postService, etc.) uses this Axios instance
- All responses follow a consistent ApiResponse format with message, success, data fields

**Q12: How do the image filters work without any external library?**

A: The image filters use native CSS filter properties and HTML Canvas API:
- 12 filter presets are defined with values for brightness, contrast, saturation, sepia, grayscale, and hue-rotate
- The preview uses CSS `filter` property on the img element for real-time preview
- When the user applies a filter, the Canvas API draws the image with `ctx.filter` set to the CSS filter string
- The canvas is exported as a JPEG base64 data URL
- No external library is needed - this works in all modern browsers

**Q13: How does the notification polling work on the frontend?**

A: The Navbar component polls for notifications using setInterval:
- On mount (and every 30 seconds), it calls two API endpoints in parallel:
  - GET /api/notifications (page 0, size 10) for recent notifications
  - GET /api/notifications/unread-count for the badge count
- The red badge shows the unread count
- Each notification shows a type-specific icon (heart for likes, comment bubble for comments, person icon for follows)
- Clicking a notification marks it as read via PUT /api/notifications/{id}/read
- "Mark all read" calls PUT /api/notifications/read-all

**Q14: How is the responsive design implemented?**

A: We use CSS media queries with three breakpoints:
- Mobile (< 768px): Sidebar is hidden, mobile bottom navigation bar appears, full-width content
- Tablet (768px - 1263px): Sidebar collapses to 72px (icons only), content shifts left by 72px
- Desktop (>= 1264px): Full 245px sidebar with icons and text labels
- The search and notification panels become full-screen overlays on mobile
- Profile page switches from horizontal to vertical layout on mobile
- Post grid maintains 3 columns at all sizes but reduces gap on mobile

---

### Backend Questions

**Q15: How do services communicate with each other?**

A: Services communicate via HTTP REST calls using Spring WebFlux WebClient with @LoadBalanced annotation:
- Post Service calls Authentication Service to fetch usernames
- Post Service calls Trending Service to update hashtag counts
- Post Service calls Follow Service to create LIKE and COMMENT notifications
- Follow Service calls Authentication Service to fetch usernames
- The @LoadBalanced annotation integrates with Consul for service discovery
- Service names (e.g., "http://authentication-service/...") are resolved to actual IP:port by Consul

**Q16: Why are notifications in the follow-service instead of a separate service?**

A: Notifications are in the follow-service because:
- The most common notification (FOLLOW) is already part of the follow domain
- It avoids creating an additional microservice for a relatively simple feature
- Follow-service already has the user relationship data needed for notifications
- Other services (post-service) send notifications via simple HTTP calls
- If notifications become complex (real-time WebSocket, email, push), it could be extracted into its own service

**Q17: How does the feed work? How are posts fetched?**

A: The home feed works as follows:
1. Frontend calls GET /api/posts/public with page and size parameters
2. Post Service queries the database for posts with privacy=PUBLIC, ordered by created_at DESC
3. For each post, the service fetches the username from auth-service via WebClient
4. The service also checks if the current user has liked each post
5. Results are returned as a Page object with pagination metadata
6. Frontend displays posts and shows a "Load More" button for the next page

**Q18: How does the view tracking work?**

A: View tracking uses the Intersection Observer API:
1. Each PostCard component has a ref on its root div
2. An IntersectionObserver watches for when the post enters the viewport (50% visible)
3. When triggered, it calls POST /api/posts/{id}/view
4. The backend checks if the same user viewed this post in the last 5 minutes
5. If not, it creates a VideoView record and increments the post's viewsCount
6. The 5-minute cooldown prevents inflating view counts from scrolling

**Q19: What is the purpose of the Trending Service?**

A: The Trending Service tracks which hashtags are popular:
- When a post is created with hashtags, the Post Service calls the Trending Service for each hashtag
- Trending Service creates or increments the postCount for that hashtag
- The frontend displays trending hashtags in a sidebar
- Users can filter trending posts by time period (all time, 24 hours, 7 days)
- Hashtags are searched by the frontend for suggestions while typing

**Q20: How does Spring Data JPA work in this project?**

A: Spring Data JPA provides the data access layer:
- Entity classes (annotated with @Entity) map to database tables
- Repository interfaces extend JpaRepository for basic CRUD operations
- Custom queries are defined using @Query annotation or method naming conventions
- Example: `findByUserIdOrderByCreatedAtDesc(Long userId)` generates SQL automatically
- JPA uses Hibernate as the ORM implementation
- `ddl-auto: update` means Hibernate automatically creates/updates tables based on entity definitions

---

### Security Questions

**Q21: How is the application secured against common attacks?**

A: Multiple security measures are in place:
- **Authentication**: JWT tokens with 24-hour expiry and HMAC-SHA256 signing
- **Password Security**: BCrypt hashing with automatic salting
- **CORS**: Only allows requests from specific frontend origins (localhost:3000)
- **CSRF**: Disabled because we use stateless JWT (no cookies/sessions)
- **Authorization**: API Gateway validates tokens before forwarding requests
- **Input Validation**: Jakarta Bean Validation on all request DTOs
- **SQL Injection**: Prevented by JPA parameterized queries (never raw SQL concatenation)
- **Account Lockout**: Login has a 3-attempt lockout with 60-second timer (frontend)

**Q22: Why is CSRF disabled?**

A: CSRF (Cross-Site Request Forgery) protection is disabled because:
- CSRF attacks exploit session cookies, which our app does not use
- We use stateless JWT tokens sent in the Authorization header
- Browsers do not automatically include the Authorization header in cross-site requests
- Therefore, CSRF attacks are not possible with our authentication method
- This is the standard practice for JWT-based REST APIs

**Q23: What is the session management strategy?**

A: The application uses STATELESS session management:
- `SessionCreationPolicy.STATELESS` is set in Spring Security config
- The server never creates or stores HTTP sessions
- Each request must include the JWT token in the Authorization header
- This makes the backend truly stateless - any instance can handle any request
- This is essential for horizontal scaling in microservices

---

### Database Questions

**Q24: How are database migrations handled?**

A: The project uses two approaches:
1. **Initial Schema**: The DDL script (`database/ddl-script.sql`) creates all databases and tables on first startup
2. **Ongoing Updates**: `spring.jpa.hibernate.ddl-auto: update` tells Hibernate to automatically create new tables and add new columns based on entity definitions, without dropping existing data
3. In production, a tool like Flyway or Liquibase would be used for version-controlled migrations

**Q25: Why are there separate databases for each service?**

A: Four separate databases are used:
- `instagram_auth` - User accounts and credentials
- `instagram_posts` - Posts, comments, likes, media, views
- `instagram_follows` - Follow relationships and notifications
- `instagram_trending` - Trending hashtag statistics

This follows the database-per-service pattern. Each service only accesses its own database. If service A needs data from service B's database, it makes an HTTP API call. This ensures loose coupling and independent deployability.

---

### DevOps Questions

**Q26: How does Docker Compose orchestrate the services?**

A: Docker Compose defines all services and their dependencies:
1. MySQL starts first with a health check (mysqladmin ping every 10s, 10 retries)
2. Consul starts in parallel (no dependencies)
3. Backend services depend on `mysql: condition: service_healthy` and `consul: condition: service_started`
4. API Gateway depends on all backend services
5. Frontend depends on API Gateway
6. A shared volume (`mysql-data`) persists database data across container restarts
7. Environment variables override application.yml values for Docker networking

**Q27: How would you scale this application?**

A: The application can be scaled in several ways:
- **Horizontal scaling**: Run multiple instances of any service. Consul and the load balancer will distribute requests.
- **Database scaling**: Read replicas for MySQL, or switch to a distributed database
- **Caching**: Add Redis for frequently accessed data (user profiles, trending hashtags)
- **Message queue**: Replace synchronous WebClient calls with RabbitMQ/Kafka for notifications
- **CDN**: Serve media files through a CDN instead of the post-service
- **Container orchestration**: Use Kubernetes instead of Docker Compose for production

---

### General Questions

**Q28: What challenges did you face during development?**

A: Key challenges included:
- **CORS issues**: The API Gateway needed careful CORS configuration and response header deduplication to prevent duplicate Access-Control-Allow-Origin headers
- **Service-to-service authentication**: Internal calls between services bypass the API Gateway, so the notification endpoint accepts request body data instead of requiring JWT headers
- **Username resolution**: Since each service has its own database, posts only store userId. Fetching usernames requires WebClient calls to auth-service, which adds latency
- **Health checks**: All services needed spring-boot-starter-actuator dependency and proper configuration for Consul to show green status
- **Frontend dependency issues**: Had to replace react-easy-crop with pure CSS filters to avoid dependency installation problems on restricted machines

**Q29: What would you improve if you had more time?**

A: Improvements would include:
- **Real-time notifications**: Replace polling with WebSocket connections
- **Message queue**: Use Kafka/RabbitMQ for async inter-service communication
- **Caching**: Add Redis for user profiles and trending data
- **Image storage**: Use cloud storage (S3/CloudFlare R2) instead of local file storage
- **Email integration**: Send actual emails for password reset
- **Direct messaging**: Add a chat feature between users
- **Stories/Reels**: Add ephemeral content like Instagram Stories
- **Rate limiting**: Add API rate limiting to prevent abuse
- **Monitoring**: Add Prometheus + Grafana for metrics and alerting
- **Testing**: Add comprehensive unit and integration tests

**Q30: How does this project demonstrate your understanding of distributed systems?**

A: This project demonstrates several distributed systems concepts:
- **Service decomposition**: Breaking a monolith into bounded contexts
- **Service discovery**: Dynamic service registration and discovery with Consul
- **API Gateway pattern**: Single entry point with cross-cutting concerns
- **Circuit breaker**: Fault tolerance and cascading failure prevention
- **Database per service**: Data isolation and ownership
- **Load balancing**: Client-side load balancing with Spring Cloud LoadBalancer
- **Inter-service communication**: Synchronous HTTP calls with WebClient
- **Health monitoring**: Actuator endpoints for service health reporting
- **Containerization**: Docker-based deployment with dependency management
- **Stateless authentication**: JWT-based auth suitable for horizontal scaling

---

*This report was prepared for the ZKinsta Instagram Clone Microservices Project.*
*Repository: https://github.com/pradhumangit341512/zkinsta*
