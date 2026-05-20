# ZKinsta - Instagram Clone Microservices Project

## Complete Project Report with Detailed Documentation

### Table of Contents

1. [Project Overview and Introduction](#1-project-overview-and-introduction)
2. [Technology Stack with Justification](#2-technology-stack-with-justification)
3. [System Architecture in Detail](#3-system-architecture-in-detail)
4. [Microservices - Deep Dive with Code](#4-microservices---deep-dive-with-code)
5. [Database Design - Complete Schema with SQL](#5-database-design---complete-schema-with-sql)
6. [Authentication Flow - Step by Step with Code](#6-authentication-flow---step-by-step-with-code)
7. [API Gateway - Routing, Filtering, and CORS](#7-api-gateway---routing-filtering-and-cors)
8. [Inter-Service Communication with WebClient](#8-inter-service-communication-with-webclient)
9. [Frontend Architecture - Complete Breakdown](#9-frontend-architecture---complete-breakdown)
10. [Frontend-Backend Integration - How They Connect](#10-frontend-backend-integration---how-they-connect)
11. [Notification System - End to End](#11-notification-system---end-to-end)
12. [Service Discovery and Health Checks with Consul](#12-service-discovery-and-health-checks-with-consul)
13. [Resilience Patterns - Circuit Breaker Explained](#13-resilience-patterns---circuit-breaker-explained)
14. [Docker and Deployment - Container Orchestration](#14-docker-and-deployment---container-orchestration)
15. [Complete API Endpoints Reference](#15-complete-api-endpoints-reference)
16. [How to Run the Project - Step by Step](#16-how-to-run-the-project---step-by-step)
17. [Project Flow Walkthroughs - End to End Scenarios](#17-project-flow-walkthroughs---end-to-end-scenarios)
18. [Viva Questions and Answers (50 Questions)](#18-viva-questions-and-answers)

---

## 1. Project Overview and Introduction

### 1.1 What is ZKinsta?

ZKinsta is a full-stack social media application inspired by Instagram, built using a microservices architecture. It demonstrates how modern cloud-native applications are designed, developed, and deployed. The project simulates a real-world production system where multiple independent services work together to deliver a seamless user experience.

### 1.2 Problem Statement

Building a monolithic social media application creates several problems:
- A single codebase becomes difficult to maintain as it grows
- A bug in one feature (e.g., trending) can crash the entire application
- Scaling is inefficient because you must scale the entire application even if only one feature needs more resources
- Different teams cannot work independently on different features

ZKinsta solves these problems by breaking the application into five independent microservices, each handling a specific domain.

### 1.3 Key Features Implemented

| Feature | Description | Service Responsible |
|---------|-------------|-------------------|
| User Registration | Create account with email, username, password | Authentication Service |
| User Login | Authenticate with JWT token | Authentication Service |
| Password Reset | Token-based password recovery | Authentication Service |
| User Profile | View and edit profile, bio, picture | Authentication Service |
| User Search | Search by username or full name | Authentication Service |
| Create Post | Upload image/video with caption, hashtags, filters | Post Service |
| Edit/Delete Post | Modify or remove own posts | Post Service |
| Like/Unlike | Like and unlike posts with animation | Post Service |
| Comments | Add, view, delete comments on posts | Post Service |
| View Tracking | Count unique views per post | Post Service |
| Image Filters | 12 Instagram-style CSS filters | Frontend (pure CSS) |
| Follow/Unfollow | Follow and unfollow users | Follow Service |
| Followers/Following | View follower and following lists | Follow Service |
| Notifications | Real-time notifications for follow, like, comment | Follow Service |
| Trending Hashtags | Track popular hashtags by post count | Trending Service |
| Hashtag Search | Search hashtags with autocomplete | Trending Service |
| Public Feed | Browse all public posts | Post Service |
| Privacy Controls | Public, Friends Only, Private posts | Post Service |
| Responsive Design | Works on mobile, tablet, desktop | Frontend |
| Dark Theme | Full dark mode UI | Frontend |

### 1.4 Project Statistics

| Metric | Value |
|--------|-------|
| Total Microservices | 5 (Auth, Post, Follow, Trending, Gateway) |
| Total Databases | 4 (one per business service) |
| Total Database Tables | 10 |
| Total REST API Endpoints | 40+ |
| Total Frontend Components | 13 React components |
| Total Frontend Service Files | 6 TypeScript service modules |
| Total CSS Files | 8 stylesheets (~2200 lines) |
| Backend Language | Java 21 |
| Frontend Language | TypeScript |
| Lines of Backend Code | ~3000+ |
| Lines of Frontend Code | ~4000+ |

---

## 2. Technology Stack with Justification

### 2.1 Backend Technologies

#### Spring Boot 3.2.4

Spring Boot is a Java framework that simplifies building production-ready applications. We chose version 3.2.4 because:
- It provides auto-configuration, reducing boilerplate code
- It includes embedded Tomcat server, so no separate server setup is needed
- It integrates seamlessly with Spring Cloud for microservices
- It supports Java 21 features

**How Spring Boot works in our project**: Each microservice is a standalone Spring Boot application with its own `@SpringBootApplication` class, `application.yml` configuration, and embedded web server. When you run `mvn spring-boot:run`, it starts a complete web server on the configured port.

#### Java 21

Java 21 is the latest Long-Term Support (LTS) version. We use it because:
- It has improved performance with virtual threads support
- Better garbage collection algorithms
- Enhanced pattern matching and record types
- Required by Spring Boot 3.x (which requires Java 17+)

#### Spring Cloud 2023.0.0

Spring Cloud provides tools for building distributed systems. Components we use:

| Component | Purpose | How It Works |
|-----------|---------|-------------|
| Spring Cloud Gateway | API routing | Routes incoming HTTP requests to correct microservice based on URL patterns |
| Spring Cloud Consul Discovery | Service registry | Services register with Consul; gateway discovers them automatically |
| Spring Cloud LoadBalancer | Load balancing | Distributes requests across multiple instances of a service |
| Spring Cloud OpenFeign | Declarative HTTP client | Enabled but not actively used (WebClient preferred) |
| Spring Cloud CircuitBreaker | Fault tolerance | Wraps service calls with circuit breaker pattern |

#### MySQL 8.0

MySQL is an open-source relational database. We chose it because:
- It is the most widely used open-source RDBMS
- It supports ACID transactions for data integrity
- It has excellent performance for read-heavy workloads (social media feeds)
- InnoDB engine provides row-level locking for concurrent access

**Database per service**: We create four separate databases:
```
instagram_auth     - User accounts, passwords, profile data
instagram_posts    - Posts, comments, likes, media files, views
instagram_follows  - Follow relationships, notifications
instagram_trending - Trending hashtag statistics
```

#### Spring Data JPA / Hibernate

Spring Data JPA is an abstraction layer over Hibernate ORM:

**How it works in our project**:
1. We define Java entity classes with `@Entity` annotation
2. JPA maps these classes to database tables automatically
3. We create Repository interfaces that extend `JpaRepository`
4. Spring generates SQL queries from method names at runtime

Example of automatic query generation:
```java
// This method name is automatically converted to SQL:
// SELECT * FROM follows WHERE following_id = ? ORDER BY created_at DESC
List<Follow> findByFollowingId(Long followingId);

// This generates:
// SELECT * FROM users WHERE username LIKE '%query%' OR full_name LIKE '%query%'
List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(String u, String f);
```

#### Spring Security + JWT (JJWT 0.12.5)

Spring Security provides authentication and authorization:
- Secures endpoints with role-based access control
- Integrates BCrypt password encoder
- Configures CORS and CSRF policies
- Adds JWT filter to validate tokens on every request

JJWT library handles JWT token creation and parsing:
- Creates tokens with `Jwts.builder()`
- Parses tokens with `Jwts.parser()`
- Signs tokens with HMAC-SHA256 algorithm

#### Resilience4j Circuit Breaker

Resilience4j is a lightweight fault tolerance library:
- Monitors failure rates of method calls
- Opens circuit when failure threshold is exceeded
- Redirects to fallback methods during outages
- Automatically recovers when service comes back

#### Spring WebFlux WebClient

WebClient is the modern HTTP client for Spring:
- Replaces deprecated RestTemplate
- Supports reactive (non-blocking) and imperative (blocking) modes
- Integrates with Spring Cloud LoadBalancer for service discovery
- Used for inter-service communication

#### Spring Boot Actuator

Actuator exposes operational endpoints:
- `/actuator/health` - Returns service health status (UP/DOWN)
- `/actuator/info` - Returns service information
- Used by Consul for health checking

#### SpringDoc OpenAPI (Swagger) 2.3.0

Generates interactive API documentation:
- Auto-generates API docs from controller annotations
- Accessible at `/swagger-ui.html` on each service
- Shows all endpoints, parameters, request/response bodies

#### ModelMapper 3.2.0

Simplifies object-to-object mapping:
- Converts entity objects to DTO objects automatically
- Maps fields by name convention
- Reduces boilerplate code for data transformation

### 2.2 Frontend Technologies

#### React 18.3.1

React is a JavaScript library for building user interfaces:
- Uses component-based architecture
- Virtual DOM for efficient UI updates
- Rich ecosystem of tools and libraries

**How React works in our project**: The UI is composed of reusable components (PostCard, Navbar, Profile, etc.). Each component manages its own state using `useState` hook. Global state (authentication) is managed via React Context API.

#### TypeScript 4.9.5

TypeScript adds static typing to JavaScript:
- Catches bugs at compile time instead of runtime
- Provides better IDE autocomplete and refactoring
- Makes code self-documenting through type definitions

**How TypeScript works in our project**: We define interfaces for all data types (User, Post, Notification, etc.) in `types/index.ts`. Every component and service function has typed parameters and return values.

#### React Router DOM 6.28.0

Handles client-side routing:
- Maps URL paths to React components
- Supports dynamic route parameters (`:username`)
- Provides `useNavigate`, `useParams`, `useLocation` hooks

#### Axios 1.7.7

HTTP client library for API calls:
- Creates a centralized API instance with base URL
- Supports request/response interceptors for token management
- Provides cleaner API than native `fetch`

### 2.3 DevOps Technologies

#### Docker

Docker packages applications into containers:
- Each microservice has its own Dockerfile
- Containers are isolated and portable
- Same image runs on any machine

#### Docker Compose

Orchestrates multiple containers:
- Defines all 8 services in a single `docker-compose.yml`
- Manages startup order with `depends_on`
- Provides networking between containers
- Manages volumes for data persistence

#### HashiCorp Consul

Service registry and health monitoring:
- Services register on startup with name, IP, port
- Provides DNS and HTTP-based service discovery
- Performs periodic health checks on all services
- Web UI at port 8500 shows service status

---

## 3. System Architecture in Detail

### 3.1 Architecture Diagram

```
+----------------------------------------------------------+
|                         CLIENT                            |
|                    (Web Browser)                           |
|                   http://localhost:3000                    |
+----------------------------+-----------------------------+
                             |
                      All HTTP Requests
                             |
                             v
+----------------------------+-----------------------------+
|                    API GATEWAY (Port 8080)                |
|                   Spring Cloud Gateway                    |
|                                                          |
|  +--------------------------------------------------+   |
|  |           JwtAuthenticationFilter                  |   |
|  |  1. Check if route needs authentication            |   |
|  |  2. Extract JWT from "Bearer <token>" header       |   |
|  |  3. Validate token signature and expiration        |   |
|  |  4. Extract username and userId from claims        |   |
|  |  5. Add X-Username and X-User-Id headers           |   |
|  +--------------------------------------------------+   |
|                                                          |
|  +--------------------------------------------------+   |
|  |              Route Configuration                   |   |
|  |  /api/auth/**    -> authentication-service         |   |
|  |  /api/posts/**   -> post-service                   |   |
|  |  /api/follows/** -> follow-service                 |   |
|  |  /api/notifications/** -> follow-service           |   |
|  |  /api/trending/** -> trending-service              |   |
|  +--------------------------------------------------+   |
|                                                          |
|  +--------------------------------------------------+   |
|  |             CORS Configuration                     |   |
|  |  Allowed Origins: localhost:3000, :3001, :3002     |   |
|  |  Allowed Methods: GET, POST, PUT, DELETE, OPTIONS  |   |
|  |  Allow Credentials: true                           |   |
|  +--------------------------------------------------+   |
+---+---------------+----------------+---------------+-----+
    |               |                |               |
    v               v                v               v
+---+---+   +------+------+  +------+------+  +----+-------+
| AUTH  |   |    POST     |  |   FOLLOW    |  |  TRENDING  |
| 8081  |   |    8082     |  |    8083     |  |    8084    |
+---+---+   +------+------+  +------+------+  +----+-------+
    |               |                |               |
    |          WebClient calls       |               |
    |<--------------+                |               |
    |     (fetch usernames)          |               |
    |               +--------------->|               |
    |               | (send notifs)  |               |
    |               +-------------------------------->|
    |               |  (update hashtags)             |
    |<-------------------------------+               |
    |     (fetch usernames)          |               |
    |               |                |               |
+---v---------------v----------------v---------------v-----+
|                    Consul (Port 8500)                     |
|              Service Registry + Health Checks             |
|  Services register: name, IP, port, health endpoint      |
|  Gateway discovers: resolves service-name to IP:port     |
|  Health checks: GET /actuator/health every 10 seconds    |
+----------------------------------------------------------+
    |               |                |               |
+---v---------------v----------------v---------------v-----+
|                    MySQL (Port 3306)                      |
|  +---------------+ +---------------+ +----------------+  |
|  |instagram_auth | |instagram_posts| |instagram_follows|  |
|  | - users       | | - posts       | | - follows       | |
|  |               | | - post_likes  | | - notifications | |
|  |               | | - post_comments| |                | |
|  |               | | - post_hashtags| |                | |
|  |               | | - media_files | |                | |
|  |               | | - video_views | |                | |
|  +---------------+ +---------------+ +----------------+  |
|                                       +----------------+  |
|                                       |instagram_trending| |
|                                       | - trending_hashtags|
|                                       +----------------+  |
+----------------------------------------------------------+
```

### 3.2 Request Flow Example

When a user likes a post, here is the complete flow through the system:

```
Step 1: User clicks heart icon on a post in the browser

Step 2: Frontend (PostCard.tsx)
  - Optimistically updates UI (heart turns red, count increments)
  - Sends: POST http://localhost:8080/api/posts/42/like
  - Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Step 3: API Gateway (Port 8080)
  - Matches route: /api/posts/** -> post-service (requires JWT)
  - JwtAuthenticationFilter:
    a. Extracts token from "Bearer eyJhbGciOiJIUzI1NiJ9..."
    b. Parses JWT claims using secret key
    c. Extracts: username="john_doe", userId=1
    d. Adds headers: X-Username: john_doe, X-User-Id: 1
  - Queries Consul for "post-service" instances
  - Gets: 192.168.1.5:8082
  - Forwards request to http://192.168.1.5:8082/api/posts/42/like

Step 4: Post Service (Port 8082)
  PostController.likePost():
  - Reads X-User-Id: 1 and X-Username: "john_doe" from headers
  - Calls PostService.likePost(42, 1, "john_doe")

  PostService.likePost():
  a. Finds post 42 in database
  b. Checks if user 1 already liked post 42 (prevents duplicate)
  c. Creates Like record: {postId: 42, userId: 1}
  d. Increments post.likesCount from 5 to 6
  e. Saves updated post to database
  f. Sends notification via WebClient:
     POST http://follow-service/api/notifications
     Body: {
       senderId: 1,
       receiverId: 7,  (post owner)
       type: "LIKE",
       message: "john_doe liked your post",
       referenceId: 42
     }

Step 5: Follow Service (Port 8083)
  NotificationController.createNotification():
  - Receives notification request body
  - Calls NotificationService.createNotification()
  - Saves Notification to database:
    {senderId: 1, receiverId: 7, type: LIKE, message: "john_doe liked your post", referenceId: 42, isRead: false}
  - Returns success response

Step 6: Response flows back
  - Follow Service -> Post Service: notification created
  - Post Service -> API Gateway: PostDto with updated likesCount=6
  - API Gateway -> Frontend: JSON response

Step 7: Frontend receives response
  - If success: UI already updated (optimistic)
  - If error: reverts UI (heart turns gray, count decrements)

Step 8: Post owner's frontend (User 7)
  - Navbar polls every 30 seconds: GET /api/notifications/unread-count
  - Gets unread count: 1
  - Red badge appears on heart icon
  - User clicks to see: "[heart icon] john_doe liked your post - 2m"
```

### 3.3 Why This Architecture?

| Principle | How We Implement It |
|-----------|-------------------|
| Single Responsibility | Each service handles one domain (auth, posts, follows, trending) |
| Loose Coupling | Services communicate via HTTP APIs, not shared databases |
| High Cohesion | Related functionality is grouped in the same service |
| Independent Deployment | Each service has its own Docker image and can be updated separately |
| Fault Isolation | If trending-service fails, users can still post and follow |
| Scalability | Can run 3 instances of post-service if feed traffic is high |
| Technology Freedom | Each service could use different database or language |

---

## 4. Microservices - Deep Dive with Code

### 4.1 Authentication Service (Port 8081)

**Database**: `instagram_auth`
**Package**: `com.instagram.auth`

#### Entity: User

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;           // BCrypt hashed, never plain text

    @Column(length = 500)
    private String bio;

    private String profilePicture;

    private String passwordResetToken;  // UUID token for password reset
    private LocalDateTime passwordResetTokenExpiry;  // 1-hour expiry

    private LocalDateTime createdAt;    // Set by @PrePersist
    private LocalDateTime updatedAt;    // Set by @PreUpdate
}
```

#### JWT Token Generation (JwtUtil.java)

The JwtUtil class handles all JWT operations. Here is the actual code:

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;  // "MySecretKeyForJWTTokenGeneration2024InstagramCloneApp"

    @Value("${jwt.expiration}")
    private long expiration;  // 86400000 (24 hours in milliseconds)

    private SecretKey getSigningKey() {
        // Convert string secret to HMAC-SHA key
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Long userId) {
        return Jwts.builder()
                .subject(username)              // Store username as subject
                .claim("userId", userId)        // Store userId as custom claim
                .issuedAt(new Date())           // Token creation time
                .expiration(new Date(System.currentTimeMillis() + expiration))  // 24h expiry
                .signWith(getSigningKey())       // Sign with HMAC-SHA256
                .compact();                      // Build the token string
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())    // Verify signature
                .build()
                .parseSignedClaims(token);       // Parse and validate expiration
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // Invalid or expired token
        }
    }
}
```

**What the generated JWT token looks like**:
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJJZCI6MSwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.abc123signature

Decoded:
Header:  {"alg":"HS256"}
Payload: {"sub":"john_doe","userId":1,"iat":1700000000,"exp":1700086400}
Signature: HMACSHA256(header.payload, secret_key)
```

#### Registration Logic (AuthService.java)

```java
@CircuitBreaker(name = "authService", fallbackMethod = "registerFallback")
public AuthResponse register(RegisterRequest request) {
    // Step 1: Validate passwords match
    if (!request.getPassword().equals(request.getConfirmPassword())) {
        throw new CustomException("Passwords do not match", HttpStatus.BAD_REQUEST);
    }

    // Step 2: Check if username is taken
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new CustomException("Username already exists", HttpStatus.CONFLICT);
    }

    // Step 3: Check if email is taken
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new CustomException("Email already exists", HttpStatus.CONFLICT);
    }

    // Step 4: Create user with BCrypt-hashed password
    User user = User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
            .build();

    // Step 5: Save to database
    userRepository.save(user);

    // Step 6: Generate JWT token
    String token = jwtUtil.generateToken(user.getUsername(), user.getId());

    // Step 7: Return token and user info
    return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .fullName(user.getFullName())
            .message("Registration successful")
            .build();
}
```

**What BCrypt does**: When the password "MyPassword123" is encoded:
```
Input:  "MyPassword123"
Output: "$2a$10$iL2gXkoS0B42ESpOyQEkFuak4mU.9yMrk7Hu1VS5sa1qhhv4Rxewm"

Structure: $2a$10$<22_char_salt><31_char_hash>
- $2a$ = BCrypt algorithm identifier
- $10$ = Cost factor (2^10 = 1024 rounds)
- Next 22 chars = Random salt
- Remaining = Hashed password
```

Every call to `encode()` produces a different output because the salt is random. But `matches()` can verify any output against the original password.

#### Login Logic

```java
@CircuitBreaker(name = "authService", fallbackMethod = "loginFallback")
public AuthResponse login(LoginRequest request) {
    // Step 1: Find user by username
    User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new CustomException("Invalid username or password", HttpStatus.UNAUTHORIZED));

    // Step 2: Compare password with BCrypt hash
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new CustomException("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    // Step 3: Generate JWT token
    String token = jwtUtil.generateToken(user.getUsername(), user.getId());

    return AuthResponse.builder()
            .token(token)
            .username(user.getUsername())
            .fullName(user.getFullName())
            .message("Login successful")
            .build();
}
```

**Security note**: The error message is the same for wrong username and wrong password ("Invalid username or password"). This prevents attackers from knowing whether a username exists in the system.

#### Password Reset Logic

```java
public String forgotPassword(String email) {
    // Step 1: Find user by email
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException("No account found with this email", HttpStatus.NOT_FOUND));

    // Step 2: Generate random UUID token
    String token = UUID.randomUUID().toString();
    // Example: "a1b2c3d4-e5f6-7890-abcd-ef1234567890"

    // Step 3: Store token with 1-hour expiry
    user.setPasswordResetToken(token);
    user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
    userRepository.save(user);

    return token;  // In production, this would be emailed to the user
}

public String resetPassword(ResetPasswordConfirm request) {
    // Step 1: Validate passwords match
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
        throw new CustomException("Passwords do not match", HttpStatus.BAD_REQUEST);
    }

    // Step 2: Find user by reset token
    User user = userRepository.findByPasswordResetToken(request.getToken())
            .orElseThrow(() -> new CustomException("Invalid or expired reset token", HttpStatus.BAD_REQUEST));

    // Step 3: Check token expiry
    if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
        throw new CustomException("Reset token has expired", HttpStatus.BAD_REQUEST);
    }

    // Step 4: Update password and clear reset token
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiry(null);
    userRepository.save(user);

    return "Password reset successful";
}
```

#### Spring Security Configuration (SecurityConfig.java)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // BCrypt with default strength (10)
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF - not needed for stateless JWT authentication
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS - allow frontend origins
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless sessions - no server-side session storage
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Define which endpoints are public vs protected
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/auth/users/search",
                    "/api/auth/users/**",
                    "/api/auth/profile/{username}",
                    "/api/auth/check-username/**",
                    "/api/auth/check-email/**",
                    "/actuator/**",
                    "/swagger-ui/**", "/v3/api-docs/**"
                ).permitAll()                    // These endpoints need no token
                .anyRequest().authenticated()     // Everything else needs a token
            )

            // Add JWT filter before Spring's default auth filter
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // JWT Filter: Validates token on every request
    @Bean
    public OncePerRequestFilter jwtAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);  // Remove "Bearer " prefix
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.extractUsername(token);
                        var userDetails = userDetailsService.loadUserByUsername(username);
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}
```

---

### 4.2 Post Service (Port 8082)

**Database**: `instagram_posts`
**Package**: `com.instagram.post`

#### Key Entities

```java
// Post Entity
@Entity
@Table(name = "posts")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String caption;
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;  // IMAGE, VIDEO, TEXT

    @Enumerated(EnumType.STRING)
    private Privacy privacy;       // PUBLIC, FRIENDS_ONLY, PRIVATE

    private String filter;         // "clarendon", "moon", etc.

    @ElementCollection
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag")
    private List<String> hashtags; // Stored in separate table

    @Builder.Default
    private Long likesCount = 0L;
    @Builder.Default
    private Long viewsCount = 0L;
}

// Like Entity
@Entity
@Table(name = "post_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class Like {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}

// Comment Entity
@Entity
@Table(name = "post_comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long userId;
    private String text;
    private LocalDateTime createdAt;
}
```

#### Inter-Service Communication: Fetching Usernames

The Post database only stores `userId` (a number), not the username. To display posts with usernames, PostService calls the Authentication Service:

```java
private String fetchUsername(Long userId) {
    try {
        // Call authentication-service via Consul service discovery
        Map<String, Object> response = webClientBuilder.build()
                .get()
                .uri("http://authentication-service/api/auth/users/{userId}", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();  // Wait for response (synchronous)

        if (response != null && response.get("data") != null) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            return (String) data.get("username");
        }
    } catch (Exception e) {
        log.warn("Failed to fetch username for userId {}: {}", userId, e.getMessage());
    }
    return "user_" + userId;  // Fallback if service is unavailable
}
```

#### Sending Notifications to Follow Service

When a user likes a post or adds a comment, PostService sends a notification:

```java
private void sendNotification(Long senderId, Long receiverId, String type,
                               String message, Long referenceId) {
    // Don't notify yourself
    if (senderId.equals(receiverId)) return;

    try {
        Map<String, Object> body = Map.of(
                "senderId", senderId,
                "receiverId", receiverId,
                "type", type,           // "LIKE" or "COMMENT"
                "message", message,     // "john_doe liked your post"
                "referenceId", referenceId  // Post ID
        );
        webClientBuilder.build()
                .post()
                .uri("http://follow-service/api/notifications")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    } catch (Exception e) {
        log.warn("Failed to send {} notification: {}", type, e.getMessage());
    }
}
```

This is called in two places:

```java
// In likePost() method:
sendNotification(userId, post.getUserId(), "LIKE",
        username + " liked your post", postId);

// In addComment() method:
sendNotification(userId, post.getUserId(), "COMMENT",
        username + " commented on your post: " +
        request.getText().substring(0, Math.min(request.getText().length(), 50)),
        postId);
```

#### View Tracking with Cooldown

```java
@Transactional
public PostDto recordView(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException("Post not found", HttpStatus.NOT_FOUND));

    // Check if this user viewed this post in the last 5 minutes
    boolean recentView = videoViewRepository.existsByPostIdAndUserIdAndViewedAtAfter(
            postId, userId, LocalDateTime.now().minusMinutes(5));

    if (!recentView) {
        // Create new view record
        VideoView view = VideoView.builder()
                .postId(postId)
                .userId(userId)
                .watchedDuration(0)
                .build();
        videoViewRepository.save(view);

        // Increment view count
        post.setViewsCount(post.getViewsCount() + 1);
        postRepository.save(post);
    }
    return mapToDto(post, userId, null);
}
```

---

### 4.3 Follow Service (Port 8083)

**Database**: `instagram_follows`
**Package**: `com.instagram.follow`

#### Follow Logic with Notification Creation

```java
@CircuitBreaker(name = "followService", fallbackMethod = "followUserFallback")
public FollowDto followUser(Long followerId, String followerUsername, FollowRequest request) {
    // Prevent self-follow
    if (followerId.equals(request.getFollowingId())) {
        throw new CustomException("You cannot follow yourself", HttpStatus.BAD_REQUEST);
    }

    // Prevent duplicate follow
    if (followRepository.existsByFollowerIdAndFollowingId(followerId, request.getFollowingId())) {
        throw new CustomException("You are already following this user", HttpStatus.CONFLICT);
    }

    // Save follow relationship
    Follow follow = Follow.builder()
            .followerId(followerId)
            .followingId(request.getFollowingId())
            .build();
    Follow saved = followRepository.save(follow);

    // Fetch the followed user's username from auth service
    String followingUsername = fetchUsername(request.getFollowingId());

    // Create FOLLOW notification
    try {
        notificationService.createNotification(CreateNotificationRequest.builder()
                .senderId(followerId)
                .receiverId(request.getFollowingId())
                .type("FOLLOW")
                .message(followerUsername + " started following you")
                .referenceId(saved.getId())
                .build());
    } catch (Exception e) {
        log.warn("Failed to create follow notification: {}", e.getMessage());
    }

    FollowDto dto = modelMapper.map(saved, FollowDto.class);
    dto.setFollowerUsername(followerUsername);
    dto.setFollowingUsername(followingUsername);
    return dto;
}
```

#### Notification Entity

```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;      // Who triggered the notification
    private Long receiverId;    // Who should see the notification

    @Enumerated(EnumType.STRING)
    private NotificationType type;  // LIKE, FOLLOW, COMMENT, NEW_POST, PASSWORD_RESET

    private String message;     // Human-readable message
    private Long referenceId;   // Post ID or Follow ID

    @Builder.Default
    private boolean read = false;  // Read/unread status

    private LocalDateTime createdAt;

    public enum NotificationType {
        LIKE, FOLLOW, NEW_POST, COMMENT, PASSWORD_RESET
    }
}
```

---

### 4.4 Trending Service (Port 8084)

**Database**: `instagram_trending`
**Package**: `com.instagram.trending`

#### Hashtag Tracking Logic

```java
@CircuitBreaker(name = "trendingService", fallbackMethod = "getTrendingHashtagsFallback")
public List<TrendingHashtagDto> getTrendingHashtags(int limit) {
    // Get top hashtags sorted by post count (descending)
    return trendingHashtagRepository
            .findAllByOrderByPostCountDesc(PageRequest.of(0, limit))
            .stream()
            .map(h -> modelMapper.map(h, TrendingHashtagDto.class))
            .collect(Collectors.toList());
}

public TrendingHashtagDto updateHashtagCount(String hashtag) {
    String normalizedTag = hashtag.toLowerCase().trim();

    // Find or create hashtag
    TrendingHashtag trending = trendingHashtagRepository.findByHashtag(normalizedTag)
            .orElse(TrendingHashtag.builder()
                    .hashtag(normalizedTag)
                    .postCount(0L)
                    .viewCount(0L)
                    .build());

    // Increment post count
    trending.setPostCount(trending.getPostCount() + 1);
    TrendingHashtag saved = trendingHashtagRepository.save(trending);
    return modelMapper.map(saved, TrendingHashtagDto.class);
}
```

---

### 4.5 API Gateway (Port 8080)

#### JWT Authentication Filter (Actual Code)

```java
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<Config> {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Skip CORS preflight requests
            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Extract and validate token
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);  // Remove "Bearer " prefix
            try {
                // Parse JWT claims
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Extract user info from token
                String username = claims.getSubject();
                String userId = String.valueOf(claims.get("userId", Long.class));

                // Add user info as headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Username", username)
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
        };
    }
}
```

**Key insight**: The downstream services (post-service, follow-service) never see the JWT token. They only see `X-Username` and `X-User-Id` headers that the gateway extracted from the token. This means:
- Only the gateway needs the JWT secret key
- Backend services trust the gateway and read headers directly
- This is more efficient than re-validating JWT in every service

---

## 5. Database Design - Complete Schema with SQL

### 5.1 Complete DDL Script

```sql
-- ==========================================
-- Database 1: Authentication Service
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_auth;
USE instagram_auth;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,       -- Each email must be unique
    username VARCHAR(255) NOT NULL UNIQUE,     -- Each username must be unique
    password VARCHAR(255) NOT NULL,            -- BCrypt hashed password
    bio VARCHAR(500),                          -- User bio (optional)
    profile_picture VARCHAR(500),              -- URL to profile picture
    password_reset_token VARCHAR(255),         -- UUID for password reset
    password_reset_token_expiry DATETIME,      -- Token expiry (1 hour)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),             -- Fast lookup by username
    INDEX idx_email (email),                   -- Fast lookup by email
    INDEX idx_reset_token (password_reset_token)  -- Fast token validation
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Database 2: Post Service
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_posts;
USE instagram_posts;

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,                   -- References users.id in instagram_auth
    username VARCHAR(255),
    caption TEXT,                               -- Post caption (up to 65535 chars)
    media_url VARCHAR(1000),                   -- URL to uploaded media
    media_type ENUM('IMAGE', 'VIDEO', 'TEXT') NOT NULL,
    privacy ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE') NOT NULL DEFAULT 'PUBLIC',
    filter VARCHAR(100),                       -- Applied filter name
    likes_count BIGINT NOT NULL DEFAULT 0,     -- Denormalized count for performance
    views_count BIGINT NOT NULL DEFAULT 0,     -- Denormalized count for performance
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),              -- Fast lookup by user
    INDEX idx_privacy (privacy),               -- Fast filtering by privacy
    INDEX idx_created_at (created_at),         -- Fast sorting by date
    INDEX idx_likes_count (likes_count)        -- Fast sorting by popularity
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Hashtags are stored in a separate table (one-to-many)
CREATE TABLE IF NOT EXISTS post_hashtags (
    post_id BIGINT NOT NULL,
    hashtag VARCHAR(255) NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_hashtag (hashtag),               -- Fast search by hashtag
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Likes table with unique constraint to prevent double-liking
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_post_user (post_id, user_id),  -- Each user can like a post only once
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comments table
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

-- Media files stored as BLOBs
CREATE TABLE IF NOT EXISTS media_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_type VARCHAR(100) NOT NULL,        -- "image/jpeg", "video/mp4"
    filename VARCHAR(500),
    data MEDIUMBLOB NOT NULL,                  -- Up to 16MB binary data
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Video views with unique constraint for cooldown tracking
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
-- Database 3: Follow Service
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_follows;
USE instagram_follows;

CREATE TABLE IF NOT EXISTS follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,               -- The user who is following
    follower_username VARCHAR(255),
    following_id BIGINT NOT NULL,              -- The user being followed
    following_username VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follower_following (follower_id, following_id),  -- Prevent duplicate follows
    INDEX idx_follower_id (follower_id),       -- Fast lookup: who do I follow?
    INDEX idx_following_id (following_id)       -- Fast lookup: who follows me?
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,                 -- Who triggered the notification
    sender_username VARCHAR(255),
    receiver_id BIGINT NOT NULL,               -- Who should see the notification
    type ENUM('LIKE', 'FOLLOW', 'NEW_POST', 'COMMENT', 'PASSWORD_RESET') NOT NULL,
    message VARCHAR(500) NOT NULL,             -- "john_doe liked your post"
    reference_id BIGINT,                       -- Post ID or Follow ID
    is_read BOOLEAN NOT NULL DEFAULT FALSE,    -- Read/unread flag
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receiver_id (receiver_id),       -- Fast lookup: my notifications
    INDEX idx_is_read (is_read),               -- Fast filtering: unread only
    INDEX idx_created_at (created_at)          -- Fast sorting: newest first
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Database 4: Trending Service
-- ==========================================
CREATE DATABASE IF NOT EXISTS instagram_trending;
USE instagram_trending;

CREATE TABLE IF NOT EXISTS trending_hashtags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hashtag VARCHAR(255) NOT NULL UNIQUE,       -- Each hashtag stored once
    post_count BIGINT NOT NULL DEFAULT 0,      -- How many posts use this hashtag
    view_count BIGINT NOT NULL DEFAULT 0,      -- How many views these posts got
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hashtag (hashtag),
    INDEX idx_post_count (post_count),         -- Fast sorting by popularity
    INDEX idx_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5.2 Why These Design Choices?

**Denormalized like/view counts**: Instead of counting likes with `SELECT COUNT(*) FROM post_likes WHERE post_id = ?` every time (slow), we store `likes_count` directly in the `posts` table and increment/decrement it. This makes feed queries fast because we don't need JOINs.

**Unique constraints**: `uk_post_user` on post_likes prevents a user from liking the same post twice. `uk_follower_following` on follows prevents duplicate follow records.

**ON DELETE CASCADE**: When a post is deleted, all its likes, comments, hashtags, and views are automatically deleted. No orphan records.

**InnoDB Engine**: Supports transactions (ACID compliance), row-level locking (better concurrent access), and foreign keys.

**utf8mb4 charset**: Supports emojis and all Unicode characters in captions and comments.

**Indexes**: Every foreign key and commonly queried column has an index for fast lookups:
- `idx_user_id` on posts: Fast "show me all posts by user X"
- `idx_created_at` on posts: Fast "sort by newest first"
- `idx_receiver_id` on notifications: Fast "show me my notifications"
- `idx_hashtag` on post_hashtags: Fast "find all posts with #travel"

---

## 6. Authentication Flow - Step by Step with Code

### 6.1 Complete Registration Flow

```
FRONTEND (Register.tsx)                                BACKEND
========================                              ========================

1. User fills form:
   fullName: "John Doe"
   email: "john@example.com"
   username: "john_doe"
   password: "Test@1234"
   confirmPassword: "Test@1234"

2. Real-time checks while typing:
   GET /api/auth/check-username/john_doe
   -> Response: { data: true }  (available)

   GET /api/auth/check-email/john@example.com
   -> Response: { data: true }  (available)

3. User clicks "Sign up"
   POST /api/auth/register
   Body: {
     "fullName": "John Doe",
     "email": "john@example.com",
     "username": "john_doe",
     "password": "Test@1234",
     "confirmPassword": "Test@1234"
   }

                                                      4. API Gateway receives request
                                                         Route: auth-public (no JWT needed)
                                                         Forwards to authentication-service

                                                      5. AuthController.register() receives request
                                                         Calls AuthService.register()

                                                      6. AuthService.register():
                                                         a. Validates passwords match ✓
                                                         b. existsByUsername("john_doe") -> false ✓
                                                         c. existsByEmail("john@example.com") -> false ✓
                                                         d. BCrypt.encode("Test@1234")
                                                            -> "$2a$10$iL2gXko..."
                                                         e. INSERT INTO users VALUES (...)
                                                         f. generateToken("john_doe", 1)
                                                            -> "eyJhbGci..."

                                                      7. Response:
                                                         {
                                                           "message": "Registration successful",
                                                           "success": true,
                                                           "data": {
                                                             "token": "eyJhbGci...",
                                                             "username": "john_doe",
                                                             "fullName": "John Doe"
                                                           }
                                                         }

8. Frontend receives response
   a. Calls login("eyJhbGci...")
   b. localStorage.setItem("token", "eyJhbGci...")
   c. setToken("eyJhbGci...")
   d. useEffect triggers: refreshProfile()
   e. GET /api/auth/profile (with Bearer token)
   f. setUser({id:1, username:"john_doe", ...})
   g. isAuthenticated becomes true
   h. ProtectedRoute allows access to /
   i. User sees home feed
```

### 6.2 Complete Login Flow

```
FRONTEND (Login.tsx)                                   BACKEND
========================                              ========================

1. User fills form:
   username: "john_doe"
   password: "Test@1234"

2. User clicks "Log in"
   POST /api/auth/login
   Body: {
     "username": "john_doe",
     "password": "Test@1234"
   }

                                                      3. API Gateway -> auth-public route
                                                         Forward to authentication-service

                                                      4. AuthService.login():
                                                         a. SELECT * FROM users
                                                            WHERE username = 'john_doe'
                                                            -> Found user (id=1)
                                                         b. BCrypt.matches(
                                                              "Test@1234",
                                                              "$2a$10$iL2gXko..."
                                                            ) -> true ✓
                                                         c. generateToken("john_doe", 1)

                                                      5. Response:
                                                         {
                                                           "data": {
                                                             "token": "eyJhbGci...",
                                                             "username": "john_doe",
                                                             "fullName": "John Doe"
                                                           }
                                                         }

6. Frontend receives response
   a. login(token) -> stores in localStorage
   b. refreshProfile() -> fetches user data
   c. Navigate to "/"

FAILED LOGIN (wrong password):
   - BCrypt.matches() returns false
   - Throws: "Invalid username or password" (401)
   - Frontend shows error message
   - After 3 failures: 60-second lockout timer (frontend-only)
```

### 6.3 How Every Authenticated Request Works

```
FRONTEND                    API GATEWAY                  POST SERVICE
========                    ===========                  ============

GET /api/posts/public
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJJZCI6MX0.sig

                            JwtAuthenticationFilter:

                            Step 1: Check route
                            /api/posts/public matches "post-public" route
                            This route has NO JwtAuthenticationFilter
                            -> Pass through without JWT validation

                            Step 2: Resolve service
                            Ask Consul: "Where is post-service?"
                            Consul returns: 192.168.1.5:8082

                            Step 3: Forward request
                            GET http://192.168.1.5:8082/api/posts/public

                                                         PostController.getPublicFeed()
                                                         X-User-Id header: null (optional)
                                                         Returns public posts

--- PROTECTED ROUTE EXAMPLE ---

POST /api/posts (Create post)
Headers:
  Authorization: Bearer eyJhbGci...

                            JwtAuthenticationFilter:

                            Step 1: Check route
                            /api/posts/** matches "post-service" route
                            This route HAS JwtAuthenticationFilter

                            Step 2: Extract token
                            "Bearer eyJhbGci..." -> "eyJhbGci..."

                            Step 3: Parse JWT
                            Jwts.parser()
                              .verifyWith(secretKey)
                              .build()
                              .parseSignedClaims("eyJhbGci...")
                            -> Claims: {sub: "john_doe", userId: 1}

                            Step 4: Add headers
                            X-Username: john_doe
                            X-User-Id: 1

                            Step 5: Forward to post-service
                            POST http://192.168.1.5:8082/api/posts
                            + X-Username: john_doe
                            + X-User-Id: 1

                                                         PostController.createPost(
                                                           @RequestHeader("X-User-Id") Long userId,    // 1
                                                           @RequestHeader("X-Username") String username // "john_doe"
                                                         )
```

---

## 7. API Gateway - Routing, Filtering, and CORS

### 7.1 Complete Route Table

| Priority | Route ID | URL Pattern | Target | JWT Filter | Why Public/Protected |
|----------|---------|-------------|--------|-----------|---------------------|
| 1 | auth-public | /api/auth/register, /api/auth/login, etc. | authentication-service | No | Users need to register/login without a token |
| 2 | auth-profile-public | /api/auth/profile/{username} (GET) | authentication-service | No | Anyone can view a public profile |
| 3 | auth-protected | /api/auth/** | authentication-service | Yes | Editing profile requires authentication |
| 4 | media-serve | /api/media/{id} (GET) | post-service | No | Media files are publicly viewable |
| 5 | media-upload | /api/media/** | post-service | Yes | Uploading requires authentication |
| 6 | post-comments-public | /api/posts/{id}/comments (GET) | post-service | No | Anyone can read comments |
| 7 | post-public | /api/posts/public, /trending, /search, /hashtag/** | post-service | No | Public content viewable without login |
| 8 | post-service | /api/posts/** | post-service | Yes | Creating, liking, commenting requires auth |
| 9 | follow-service | /api/follows/** | follow-service | Yes | All follow operations require auth |
| 10 | notification-service | /api/notifications/** | follow-service | Yes | Notifications are personal and private |
| 11 | trending-service | /api/trending/** | trending-service | No | Trending data is public information |

**Route priority matters**: Routes are evaluated in order. More specific routes (like `auth-public` with exact paths) are listed before catch-all routes (like `auth-protected` with `/**`). This ensures `/api/auth/register` matches the public route, not the protected one.

### 7.2 CORS Explained

CORS (Cross-Origin Resource Sharing) is needed because:
- Frontend runs on `http://localhost:3000`
- API Gateway runs on `http://localhost:8080`
- Browsers block requests from one origin to another by default

Our CORS configuration:
```yaml
allowedOrigins:
  - "http://localhost:3000"    # React dev server
  - "http://localhost:3001"    # Alternative port
  - "http://localhost:3002"    # Alternative port
allowedMethods: GET, POST, PUT, DELETE, OPTIONS
allowedHeaders: "*"            # Accept any header (including Authorization)
allowCredentials: true         # Allow cookies and auth headers
maxAge: 3600                   # Cache preflight response for 1 hour
```

**Preflight request**: Before sending a POST/PUT/DELETE request, the browser sends an OPTIONS request to check if CORS is allowed. The gateway responds with CORS headers, and then the browser sends the actual request. The JWT filter skips OPTIONS requests.

### 7.3 Deduplication Filter

```yaml
default-filters:
  - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_UNIQUE
```

This prevents duplicate CORS headers. Without it, both the gateway and the downstream service might add CORS headers, causing browsers to reject the response.

---

## 8. Inter-Service Communication with WebClient

### 8.1 How @LoadBalanced WebClient Works

```java
// In PostServiceApplication.java:
@Bean
@LoadBalanced  // This annotation enables Consul service discovery
public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
}
```

When PostService calls:
```java
webClientBuilder.build()
    .get()
    .uri("http://authentication-service/api/auth/users/1")
    .retrieve()
    .bodyToMono(Map.class)
    .block();
```

Here is what happens internally:
1. `webClientBuilder.build()` creates a WebClient with LoadBalancer support
2. `"http://authentication-service"` is NOT a real hostname
3. Spring Cloud LoadBalancer intercepts this URL
4. It queries Consul: "Give me instances of authentication-service"
5. Consul returns: `[{host: "192.168.1.5", port: 8081}]`
6. LoadBalancer picks one instance (round-robin)
7. The actual HTTP request goes to: `http://192.168.1.5:8081/api/auth/users/1`
8. `.bodyToMono(Map.class)` reads the JSON response into a Map
9. `.block()` waits for the response (synchronous/blocking call)

### 8.2 All Inter-Service Communication Paths

```
+-------------------+          +-------------------+
| Post Service      |  GET     | Auth Service      |
| (Port 8082)       |--------->| (Port 8081)       |
|                   | /api/auth/users/{userId}      |
| Purpose: Get      |          | Returns: username, |
| username for      |          | email, fullName    |
| displaying posts  |          |                    |
+-------------------+          +-------------------+

+-------------------+          +-------------------+
| Post Service      |  POST    | Trending Service  |
| (Port 8082)       |--------->| (Port 8084)       |
|                   | /api/trending/hashtags/{tag}  |
| Purpose: Update   |          | Increments:        |
| hashtag counts    |          | postCount += 1     |
| when post created |          |                    |
+-------------------+          +-------------------+

+-------------------+          +-------------------+
| Post Service      |  POST    | Follow Service    |
| (Port 8082)       |--------->| (Port 8083)       |
|                   | /api/notifications            |
| Purpose: Send     |          | Creates LIKE or    |
| LIKE and COMMENT  |          | COMMENT notification|
| notifications     |          |                    |
+-------------------+          +-------------------+

+-------------------+          +-------------------+
| Follow Service    |  GET     | Auth Service      |
| (Port 8083)       |--------->| (Port 8081)       |
|                   | /api/auth/users/{userId}      |
| Purpose: Get      |          | Returns: username   |
| usernames for     |          |                    |
| follow lists      |          |                    |
+-------------------+          +-------------------+
```

### 8.3 Error Handling in Inter-Service Calls

All WebClient calls are wrapped in try-catch:

```java
private String fetchUsername(Long userId) {
    try {
        // Make the call...
        return (String) data.get("username");
    } catch (Exception e) {
        log.warn("Failed to fetch username for userId {}: {}", userId, e.getMessage());
    }
    return "user_" + userId;  // Graceful fallback
}
```

**Why this matters**: If authentication-service is down, post-service still works. Posts will show "user_42" instead of "john_doe" as the username. The user experience degrades gracefully instead of crashing.

---

## 9. Frontend Architecture - Complete Breakdown

### 9.1 How React Context Works for Authentication

```typescript
// AuthContext.tsx - Simplified explanation

// Step 1: Create a context with authentication data
const AuthContext = createContext<AuthContextType>({} as AuthContextType);

// Step 2: Create a custom hook for easy access
export const useAuth = () => useContext(AuthContext);

// Step 3: Create the provider that wraps the entire app
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));

    // When token changes, fetch user profile
    useEffect(() => {
        if (token) refreshProfile();
    }, [token]);

    const login = async (newToken) => {
        localStorage.setItem('token', newToken);  // Persist across page refreshes
        setToken(newToken);                         // Triggers useEffect -> refreshProfile
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    };

    const refreshProfile = async () => {
        const res = await authService.getProfile();
        setUser(res.data.data);
    };

    return (
        <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, login, logout, refreshProfile }}>
            {children}
        </AuthContext.Provider>
    );
};
```

**How components use it**:
```typescript
// Any component can access auth state:
const { user, isAuthenticated, logout } = useAuth();

if (isAuthenticated) {
    return <p>Welcome, {user.username}</p>;
}
```

### 9.2 How Axios Interceptors Work

```typescript
// api.ts - HTTP client setup

// Create Axios instance pointing to API Gateway
const api = axios.create({
    baseURL: 'http://localhost:8080',
    headers: { 'Content-Type': 'application/json' },
    timeout: 10000,  // 10 second timeout
});

// REQUEST INTERCEPTOR: Runs BEFORE every request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        // Automatically add JWT token to every request
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// RESPONSE INTERCEPTOR: Runs AFTER every response
api.interceptors.response.use(
    (response) => response,  // Success: pass through
    (error) => {
        if (error.response?.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('token');
            window.location.href = '/login';  // Force redirect to login
        }
        return Promise.reject(error);
    }
);
```

**This means**: No component ever needs to manually add tokens or handle 401 errors. It is all centralized.

### 9.3 How Image Filters Work (Pure CSS)

The image filter system uses NO external libraries. Here is how it works:

**Step 1: Define filter presets as CSS filter values**
```typescript
const FILTER_PRESETS = [
    { name: 'normal', brightness: 100, contrast: 100, saturate: 100, sepia: 0, grayscale: 0, hueRotate: 0 },
    { name: 'clarendon', brightness: 110, contrast: 120, saturate: 130, sepia: 0, grayscale: 0, hueRotate: 0 },
    { name: 'moon', brightness: 110, contrast: 110, saturate: 0, sepia: 0, grayscale: 100, hueRotate: 0 },
    // ... 12 presets total
];
```

**Step 2: Apply as CSS filter on preview**
```html
<img
    src={imageUrl}
    style={{
        filter: `brightness(110%) contrast(120%) saturate(130%) sepia(0%) grayscale(0%) hue-rotate(0deg)`
    }}
/>
```

**Step 3: Apply permanently using Canvas API**
```typescript
export async function getCroppedImg(imageSrc, pixelCrop, brightness, contrast, ...) {
    const image = await createImage(imageSrc);
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    canvas.width = pixelCrop.width;
    canvas.height = pixelCrop.height;

    // Apply the same CSS filter string to the canvas context
    ctx.filter = `brightness(${brightness}%) contrast(${contrast}%) saturate(${saturate}%)
                  sepia(${sepia}%) grayscale(${grayscale}%) hue-rotate(${hueRotate}deg)`;

    // Draw the image with the filter applied
    ctx.drawImage(image, ...);

    // Export as JPEG base64 data URL
    return canvas.toDataURL('image/jpeg', 0.9);
}
```

**Why this works on any system**: CSS `filter` and Canvas `ctx.filter` are built into every modern browser. No npm packages needed.

### 9.4 How View Tracking Works (IntersectionObserver)

```typescript
// PostCard.tsx
useEffect(() => {
    const el = cardRef.current;  // Reference to the post's DOM element
    if (!el || !user || viewRecorded.current) return;

    // Create an observer that watches when the element enters the viewport
    const observer = new IntersectionObserver(
        ([entry]) => {
            // entry.isIntersecting = true when 50% of the post is visible
            if (entry.isIntersecting && !viewRecorded.current) {
                viewRecorded.current = true;  // Only record once per component mount
                postService.recordView(post.id)
                    .then((res) => {
                        if (res.data.data) setViewsCount(res.data.data.viewsCount);
                    })
                    .catch(() => {});
            }
        },
        { threshold: 0.5 }  // Trigger when 50% visible
    );

    observer.observe(el);
    return () => observer.disconnect();  // Cleanup on unmount
}, [post.id, user]);
```

**How IntersectionObserver works**:
1. It watches DOM elements for visibility changes
2. When the user scrolls and a post becomes 50% visible, the callback fires
3. We call the backend to record the view
4. The `viewRecorded` ref prevents recording the same view multiple times
5. The backend has its own 5-minute cooldown per user per post

---

## 10. Frontend-Backend Integration - How They Connect

### 10.1 Service Layer Pattern

Each frontend service file maps to a backend microservice:

```typescript
// authService.ts
export const authService = {
    register: (data) => api.post('/api/auth/register', data),
    login: (data) => api.post('/api/auth/login', data),
    getProfile: () => api.get('/api/auth/profile'),
    getProfileByUsername: (username) => api.get(`/api/auth/profile/${username}`),
    updateProfile: (data) => api.put('/api/auth/profile', data),
    searchUsers: (query) => api.get(`/api/auth/users/search?query=${query}`),
    // ...
};

// postService.ts
export const postService = {
    createPost: (data) => api.post('/api/posts', data),
    likePost: (postId) => api.post(`/api/posts/${postId}/like`),
    unlikePost: (postId) => api.delete(`/api/posts/${postId}/like`),
    addComment: (postId, text) => api.post(`/api/posts/${postId}/comments`, { text }),
    getComments: (postId, page, size) => api.get(`/api/posts/${postId}/comments?page=${page}&size=${size}`),
    // ...
};

// followService.ts
export const followService = {
    followUser: (followingId, username) => api.post('/api/follows', { followingId, followingUsername: username }),
    unfollowUser: (followingId) => api.delete(`/api/follows/${followingId}`),
    getFollowCounts: (userId) => api.get(`/api/follows/count/${userId}`),
    isFollowing: (followingId) => api.get(`/api/follows/check?followingId=${followingId}`),
    // ...
};

// notificationService.ts
export const notificationService = {
    getNotifications: (page, size) => api.get(`/api/notifications?page=${page}&size=${size}`),
    getUnreadCount: () => api.get('/api/notifications/unread-count'),
    markAsRead: (id) => api.put(`/api/notifications/${id}/read`),
    markAllAsRead: () => api.put('/api/notifications/read-all'),
};
```

### 10.2 Optimistic UI Updates

When a user likes a post, we update the UI immediately without waiting for the server response:

```typescript
const handleLike = async () => {
    const prevLiked = liked;
    const prevCount = likesCount;

    try {
        if (liked) {
            // OPTIMISTIC UPDATE: Update UI immediately
            setLiked(false);
            setLikesCount(prev => prev - 1);

            // Then make the API call
            await postService.unlikePost(post.id);
        } else {
            // OPTIMISTIC UPDATE
            setLiked(true);
            setLikesCount(prev => prev + 1);
            setLikeAnimating(true);  // Trigger heart animation

            await postService.likePost(post.id);
        }
    } catch {
        // ROLLBACK: If API call fails, revert to previous state
        setLiked(prevLiked);
        setLikesCount(prevCount);
    }
};
```

**Why optimistic updates**: Without them, the user would see a 200-500ms delay between clicking the heart and seeing it turn red. With them, the response feels instant.

---

## 11. Notification System - End to End

### 11.1 Three Notification Triggers

| Trigger | Where Created | How Delivered |
|---------|--------------|---------------|
| Follow | FollowService.followUser() | Direct call to NotificationService |
| Like | PostService.likePost() | WebClient call to follow-service |
| Comment | PostService.addComment() | WebClient call to follow-service |

### 11.2 Notification Data Flow

```
LIKE NOTIFICATION:

1. User A clicks heart on User B's post

2. PostService.likePost():
   - Save like record
   - Increment likesCount
   - Call sendNotification(
       senderId: A,
       receiverId: B,        // Post owner
       type: "LIKE",
       message: "userA liked your post",
       referenceId: postId
     )

3. sendNotification() via WebClient:
   POST http://follow-service/api/notifications
   Body: {
     "senderId": 1,
     "receiverId": 7,
     "type": "LIKE",
     "message": "john_doe liked your post",
     "referenceId": 42
   }

4. NotificationController receives request:
   - No X-User-Id header needed (internal call)
   - Reads data from request body

5. NotificationService.createNotification():
   INSERT INTO notifications (sender_id, receiver_id, type, message, reference_id, is_read)
   VALUES (1, 7, 'LIKE', 'john_doe liked your post', 42, false)

6. User B's frontend polls every 30 seconds:
   GET /api/notifications/unread-count
   -> { data: 1 }    (badge shows "1")

   GET /api/notifications?page=0&size=10
   -> { data: { content: [{
       id: 15,
       senderId: 1,
       senderUsername: "john_doe",
       receiverId: 7,
       type: "LIKE",
       message: "john_doe liked your post",
       referenceId: 42,
       read: false,
       createdAt: "2024-01-15T10:30:00"
     }]}}

7. Navbar displays:
   [red heart icon] john_doe liked your post  -  2m

8. User B clicks the notification:
   PUT /api/notifications/15/read
   -> Notification marked as read
   -> Navigate to /profile/john_doe
```

### 11.3 Frontend Notification Rendering

```typescript
// In Navbar.tsx:
const getNotificationIcon = (type) => {
    switch (type) {
        case 'LIKE':    return <HeartIcon color="#ed4956" />;    // Red heart
        case 'COMMENT': return <CommentIcon color="#0095f6" />;  // Blue comment
        case 'FOLLOW':  return <PersonIcon color="#58c322" />;   // Green person
        default:        return <InfoIcon color="#a8a8a8" />;     // Gray info
    }
};

// Rendered as:
<div className="notif-item unread" onClick={() => handleNotificationClick(n)}>
    <div className="notif-item-icon">
        {getNotificationIcon(n.type)}
    </div>
    <div className="notif-item-content">
        <span className="notif-item-message">{n.message}</span>
        <span className="notif-item-time">{timeAgo(n.createdAt)}</span>
    </div>
</div>
```

---

## 12. Service Discovery and Health Checks with Consul

### 12.1 How Consul Service Discovery Works

**On Service Startup**:
```
1. Authentication Service starts on port 8081
2. Spring Cloud Consul auto-configuration kicks in
3. Registers with Consul:
   {
     "Name": "authentication-service",
     "ID": "authentication-service:a1b2c3d4",
     "Address": "192.168.1.5",
     "Port": 8081,
     "Check": {
       "HTTP": "http://192.168.1.5:8081/actuator/health",
       "Interval": "10s"
     }
   }
4. Consul UI (http://localhost:8500) shows the service
```

**Health Check Cycle**:
```
Every 10 seconds, Consul sends:
  GET http://192.168.1.5:8081/actuator/health

Healthy response (GREEN):
  HTTP 200 OK
  {
    "status": "UP",
    "components": {
      "db": { "status": "UP" },
      "diskSpace": { "status": "UP" }
    }
  }

Unhealthy response (RED):
  HTTP 503 or timeout
  {
    "status": "DOWN",
    "components": {
      "db": { "status": "DOWN", "details": { "error": "Connection refused" } }
    }
  }
```

**On Service Request (Gateway)**:
```
1. Gateway receives: GET /api/posts/public
2. Gateway needs to find "post-service"
3. Queries Consul: GET /v1/health/service/post-service?passing=true
4. Consul returns: [
     { "Service": { "Address": "192.168.1.5", "Port": 8082 } },
     { "Service": { "Address": "192.168.1.6", "Port": 8082 } }
   ]
5. LoadBalancer picks one (round-robin)
6. Request forwarded to http://192.168.1.5:8082/api/posts/public
```

### 12.2 Configuration Required

Each service needs three things for Consul to work:

**1. Maven dependency** (pom.xml):
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**2. Consul config** (application.yml):
```yaml
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: post-service
        instance-id: ${spring.application.name}:${random.value}
        prefer-ip-address: true
```

**3. Actuator config** (application.yml):
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

Without the actuator dependency or health endpoint, Consul cannot verify the service is running, and marks it as unhealthy (red).

---

## 13. Resilience Patterns - Circuit Breaker Explained

### 13.1 What Problem Does Circuit Breaker Solve?

Without a circuit breaker:
```
User creates a post
-> PostService calls auth-service to fetch username
-> auth-service is DOWN
-> WebClient waits for timeout (30 seconds)
-> User waits 30 seconds for an error
-> 100 users do this simultaneously
-> PostService runs out of threads
-> PostService becomes unresponsive
-> ENTIRE SYSTEM CASCADING FAILURE
```

With a circuit breaker:
```
User creates a post
-> PostService calls auth-service (fails)
-> After 5 failures, circuit OPENS
-> Next 100 users get instant fallback response
-> PostService stays healthy
-> When auth-service recovers, circuit CLOSES
-> Normal operation resumes
```

### 13.2 Circuit Breaker Configuration Explained

```yaml
resilience4j:
  circuitbreaker:
    instances:
      postService:
        register-health-indicator: true        # Show in /actuator/health
        sliding-window-size: 10                # Track last 10 calls
        minimum-number-of-calls: 5             # Need at least 5 calls before evaluating
        failure-rate-threshold: 50             # Open circuit if 50%+ calls fail
        wait-duration-in-open-state: 60s       # Stay open for 60 seconds
        permitted-number-of-calls-in-half-open-state: 3  # Allow 3 test calls
        sliding-window-type: COUNT_BASED       # Count-based (not time-based)
        slow-call-duration-threshold: 3s       # Calls taking >3s count as slow
```

### 13.3 Circuit Breaker State Machine

```
                     failure rate < 50%
         +--------------------------------------+
         |                                      |
         v                                      |
    +--------+    failure rate >= 50%     +------+--+
    | CLOSED |-------------------------->|  OPEN    |
    | (Normal)|                          | (Reject) |
    +--------+                           +----+-----+
         ^                                    |
         |     all 3 test calls succeed       |
         +------------------------------------+
         |                                    |
         |        60 seconds elapsed          |
         |                               +----v------+
         +-------------------------------| HALF-OPEN |
              any test call fails         | (Testing) |
                                         +-----------+
```

### 13.4 Fallback Method Example

```java
// Normal method - tries to create a post
@CircuitBreaker(name = "postService", fallbackMethod = "createPostFallback")
public PostDto createPost(Long userId, String username, CreatePostRequest request) {
    // Normal logic - may call other services via WebClient
    Post saved = postRepository.save(post);
    updateTrendingHashtags(saved.getHashtags());  // Calls trending-service
    return mapToDto(saved, userId, username);      // Calls auth-service
}

// Fallback method - called when circuit is OPEN or method throws exception
public PostDto createPostFallback(Long userId, String username, CreatePostRequest request, Throwable t) {
    // Re-throw business exceptions (like "Post not found")
    if (t instanceof CustomException) throw (CustomException) t;

    // For infrastructure failures, return user-friendly message
    log.error("Circuit breaker fallback for createPost: {}", t.getMessage());
    throw new CustomException(
        "Service temporarily unavailable. Please try again later.",
        HttpStatus.SERVICE_UNAVAILABLE  // 503
    );
}
```

---

## 14. Docker and Deployment - Container Orchestration

### 14.1 Docker Compose Service Dependencies

```
mysql ──────────> (healthcheck: mysqladmin ping)
    |
    v
consul ─────────> (starts immediately)
    |
    ├──> authentication-service (waits for mysql:healthy + consul:started)
    ├──> post-service           (waits for mysql:healthy + consul:started)
    ├──> follow-service         (waits for mysql:healthy + consul:started)
    └──> trending-service       (waits for mysql:healthy + consul:started)
            |
            v
    api-gateway ────────> (waits for ALL backend services)
            |
            v
    frontend ───────────> (waits for api-gateway)
```

### 14.2 Environment Variable Overrides

In Docker, services run in containers with their own network. `localhost` inside a container refers to the container itself, not the host machine. So we override URLs:

| Service | Local Config | Docker Override |
|---------|-------------|----------------|
| Database URL | `jdbc:mysql://localhost:3306/...` | `jdbc:mysql://mysql:3306/...` |
| Consul Host | `localhost` | `consul` |
| Media Base URL | `http://localhost:8080` | `http://localhost:8080` (accessed from browser) |

### 14.3 Data Persistence

```yaml
volumes:
  mysql-data:  # Named volume persists data across container restarts
```

Without this volume, all database data would be lost when containers stop. The volume stores MySQL data on the host machine's disk.

---

## 15. Complete API Endpoints Reference

### Authentication Service (Port 8081, Path: /api/auth)

| Method | Endpoint | Auth | Request Body | Response |
|--------|---------|------|-------------|----------|
| POST | /register | No | `{fullName, email, username, password, confirmPassword}` | `{token, username, fullName}` |
| POST | /login | No | `{username, password}` | `{token, username, fullName}` |
| GET | /profile | Yes | - | `{id, fullName, email, username, bio, profilePicture}` |
| GET | /profile/{username} | No | - | `{id, fullName, email, username, bio, profilePicture}` |
| PUT | /profile | Yes | `{fullName?, bio?, profilePicture?}` | Updated user profile |
| POST | /forgot-password | No | `{email}` | Reset token string |
| POST | /reset-password | No | `{token, newPassword, confirmPassword}` | Success message |
| GET | /users/search?query= | No | - | List of matching users |
| GET | /users/{userId} | No | - | User profile |
| GET | /check-username/{username} | No | - | Boolean (true=available) |
| GET | /check-email/{email} | No | - | Boolean (true=available) |

### Post Service (Port 8082, Path: /api/posts)

| Method | Endpoint | Auth | Request Body | Response |
|--------|---------|------|-------------|----------|
| POST | / | Yes | `{caption, mediaUrl, mediaType, privacy, hashtags, filter}` | Created post |
| GET | /{postId} | Optional | - | Post with likes, comments count |
| GET | /user/{userId} | Optional | - | List of user's posts |
| GET | /feed?followingIds=&page=&size= | Yes | - | Paginated feed |
| GET | /public?page=&size= | Optional | - | Paginated public feed |
| PUT | /{postId} | Yes | `{caption?, privacy?, filter?, hashtags?}` | Updated post |
| DELETE | /{postId} | Yes | - | Success message |
| POST | /{postId}/like | Yes | - | Post with updated likesCount |
| DELETE | /{postId}/like | Yes | - | Post with updated likesCount |
| GET | /trending?page=&size= | Optional | - | Posts sorted by likes+views |
| GET | /trending/recent?hours=&page=&size= | Optional | - | Recent trending posts |
| GET | /hashtag/{hashtag}?page=&size= | Optional | - | Posts with specific hashtag |
| GET | /search?query=&page=&size= | Optional | - | Search by caption/hashtag |
| POST | /{postId}/view | Yes | - | Post with updated viewsCount |
| POST | /{postId}/comments | Yes | `{text}` | Created comment |
| GET | /{postId}/comments?page=&size= | No | - | Paginated comments |
| DELETE | /comments/{commentId} | Yes | - | Success message |

### Follow Service (Port 8083, Path: /api/follows)

| Method | Endpoint | Auth | Request Body | Response |
|--------|---------|------|-------------|----------|
| POST | / | Yes | `{followingId, followingUsername}` | Follow record |
| DELETE | /{followingId} | Yes | - | Success message |
| GET | /followers/{userId} | Yes | - | List of followers |
| GET | /following/{userId} | Yes | - | List of following |
| GET | /count/{userId} | Yes | - | `{followersCount, followingCount}` |
| GET | /check?followingId= | Yes | - | Boolean |
| GET | /following-ids/{userId} | Yes | - | List of user IDs |

### Notification Endpoints (Port 8083, Path: /api/notifications)

| Method | Endpoint | Auth | Request Body | Response |
|--------|---------|------|-------------|----------|
| POST | / | Internal | `{senderId, receiverId, type, message, referenceId}` | Created notification |
| GET | /?page=&size= | Yes | - | Paginated notifications |
| GET | /unread-count | Yes | - | Number |
| PUT | /{id}/read | Yes | - | Success message |
| PUT | /read-all | Yes | - | Success message |

### Trending Service (Port 8084, Path: /api/trending)

| Method | Endpoint | Auth | Request Body | Response |
|--------|---------|------|-------------|----------|
| GET | /hashtags?limit= | No | - | List of trending hashtags |
| POST | /hashtags/{hashtag} | Internal | - | Updated hashtag |
| POST | /hashtags/{hashtag}/view | No | - | Updated hashtag |
| GET | /hashtags/search?query= | No | - | List of matching hashtags |

---

## 16. How to Run the Project - Step by Step

### Option 1: Docker Compose (Recommended - One Command)

```bash
# Step 1: Clone the repository
git clone https://github.com/pradhumangit341512/zkinsta.git
cd zkinsta

# Step 2: Start everything
docker-compose up --build
# This will:
# - Download MySQL 8.0 and Consul images
# - Build Docker images for all 5 backend services and frontend
# - Start MySQL with health check
# - Start Consul for service discovery
# - Start all backend services (wait for MySQL to be healthy)
# - Start API Gateway (wait for all backend services)
# - Start Frontend (wait for API Gateway)

# Step 3: Wait for all services to be healthy (~60-90 seconds)

# Step 4: Access the application
# Frontend:           http://localhost:3000
# API Gateway:        http://localhost:8080
# Consul UI:          http://localhost:8500
# Swagger (Auth):     http://localhost:8081/swagger-ui.html
# Swagger (Posts):    http://localhost:8082/swagger-ui.html
# Swagger (Follows):  http://localhost:8083/swagger-ui.html
# Swagger (Trending): http://localhost:8084/swagger-ui.html

# Step 5: Test with sample users
# Username: johndoe    Password: Test@1234
# Username: janesmith  Password: Test@1234

# To stop:
docker-compose down

# To stop and delete data:
docker-compose down -v
```

### Option 2: Local Development (Manual Setup)

```bash
# Prerequisites:
# - Java 21 JDK installed
# - Maven installed
# - MySQL 8.0 running on port 3306 (username: root, password: root)
# - Node.js v18+ installed
# - Consul installed

# Step 1: Create databases
mysql -u root -proot < database/ddl-script.sql

# Step 2: Start Consul (in its own terminal)
consul agent -dev -ui

# Step 3: Start Authentication Service (terminal 1)
cd authentication-service
mvn spring-boot:run

# Step 4: Start Post Service (terminal 2)
cd post-service
mvn spring-boot:run

# Step 5: Start Follow Service (terminal 3)
cd follow-service
mvn spring-boot:run

# Step 6: Start Trending Service (terminal 4)
cd trending-service
mvn spring-boot:run

# Step 7: Start API Gateway (terminal 5)
cd api-gateway
mvn spring-boot:run

# Step 8: Start Frontend (terminal 6)
cd frontend
npm install
npm start

# Step 9: Open http://localhost:3000
```

### Verification Checklist

| Check | Expected Result | How to Verify |
|-------|----------------|---------------|
| Consul services green | All 5 services show green | Open http://localhost:8500 |
| Frontend loads | Login page appears | Open http://localhost:3000 |
| Registration works | Token returned, redirected to home | Fill register form |
| Login works | Token returned, redirected to home | Login with johndoe / Test@1234 |
| Create post works | Post appears in feed | Upload image with caption |
| Like works | Heart turns red, count increments | Click heart icon |
| Comment works | Comment appears below post | Type and submit comment |
| Follow works | Button changes to "Following" | Visit profile, click Follow |
| Notification appears | Red badge shows on heart icon | Like someone's post, check their notifications |
| Search works | Results show users and hashtags | Type in search bar |
| Trending works | Hashtags shown in sidebar | Visit /trending page |

---

## 17. Project Flow Walkthroughs - End to End Scenarios

### 17.1 Complete Registration to First Post

```
1. USER OPENS BROWSER
   URL: http://localhost:3000
   App.tsx loads -> AuthProvider checks localStorage -> no token
   ProtectedRoute for "/" -> isAuthenticated=false -> redirect to /login

2. USER CLICKS "SIGN UP"
   Navigate to /register
   Register.tsx renders with form fields

3. USER FILLS REGISTRATION FORM
   Typing username "john_doe":
   -> After 300ms debounce:
      GET /api/auth/check-username/john_doe
      -> { data: true } -> Green "Available" text

   Typing email "john@example.com":
   -> GET /api/auth/check-email/john@example.com
      -> { data: true } -> Green "Available" text

   Password: "Test@1234"
   Confirm: "Test@1234" -> Passwords match (green)

4. USER CLICKS "SIGN UP" BUTTON
   POST /api/auth/register
   Body: { fullName, email, username, password, confirmPassword }

   Backend:
   - Validates passwords match
   - Checks username/email not taken
   - BCrypt hashes password
   - Saves user to database (id=1)
   - Generates JWT: { sub: "john_doe", userId: 1, exp: +24h }

   Response: { data: { token: "eyJ...", username: "john_doe" } }

   Frontend:
   - login("eyJ...") -> localStorage.setItem("token", "eyJ...")
   - useEffect triggers refreshProfile()
   - GET /api/auth/profile -> setUser({ id:1, username:"john_doe", ... })
   - isAuthenticated becomes true
   - Navigate to "/"

5. HOME FEED LOADS
   HomeFeed.tsx renders
   GET /api/posts/public?page=0&size=20

   Backend:
   - SELECT * FROM posts WHERE privacy='PUBLIC' ORDER BY created_at DESC LIMIT 20
   - For each post: fetchUsername(post.userId) via WebClient to auth-service
   - For each post: check if current user liked it

   Response: { data: { content: [...posts], totalPages: 1, ... } }

   Frontend renders PostCard for each post

6. USER CREATES A POST
   Click "Create" in sidebar -> Navigate to /create-post

   Select image file (dog.jpg, 2MB)
   - Validates: file < 10MB, is image type
   - FileReader reads file as DataURL for preview

   Click "Edit Photo" -> MediaEditor opens
   - Select "Clarendon" filter (brightness:110, contrast:120, saturate:130)
   - Adjust brightness slider to 115
   - Click "Apply"
   - Canvas draws image with filter applied
   - Returns base64 JPEG data URL

   Write caption: "My cute dog!"
   Add hashtags: "dog, pets, cute"
   Privacy: "Public"

   Click "Share Post":
   a. Upload image:
      POST /api/media/upload (multipart/form-data)
      -> { data: { mediaUrl: "/api/media/5" } }

   b. Create post:
      POST /api/posts
      Body: {
        caption: "My cute dog!",
        mediaUrl: "/api/media/5",
        mediaType: "IMAGE",
        privacy: "PUBLIC",
        hashtags: ["dog", "pets", "cute"],
        filter: "clarendon"
      }

      Backend:
      - Save post to database
      - For each hashtag, call trending-service:
        POST http://trending-service/api/trending/hashtags/dog
        POST http://trending-service/api/trending/hashtags/pets
        POST http://trending-service/api/trending/hashtags/cute

      Response: { data: { id: 1, caption: "My cute dog!", ... } }

   c. Show success message, redirect to "/"
   d. New post appears at top of feed
```

### 17.2 Like, Comment, and Notification Flow

```
SETUP: User A (john_doe, id=1) and User B (jane_smith, id=2)
User B has a post (id=10) about "Beautiful sunset"

1. USER A SCROLLS THROUGH FEED
   PostCard for post 10 enters viewport (50% visible)
   IntersectionObserver fires:
   POST /api/posts/10/view (X-User-Id: 1)
   Backend: No recent view -> creates view record, viewsCount: 0 -> 1

2. USER A LIKES THE POST
   Double-tap or click heart icon
   Optimistic update: heart turns red, likesCount: 5 -> 6
   POST /api/posts/10/like (X-User-Id: 1, X-Username: john_doe)

   Backend PostService.likePost():
   a. Find post 10 (userId=2, "Beautiful sunset")
   b. Check: likeRepository.existsByPostIdAndUserId(10, 1) -> false (not yet liked)
   c. Save: Like { postId: 10, userId: 1 }
   d. Update: post.likesCount = 5 + 1 = 6
   e. Send notification:
      sendNotification(1, 2, "LIKE", "john_doe liked your post", 10)
      -> POST http://follow-service/api/notifications
      -> { senderId: 1, receiverId: 2, type: "LIKE", message: "...", referenceId: 10 }

   Response: { data: { id: 10, likesCount: 6, likedByCurrentUser: true, ... } }

3. USER A COMMENTS
   Click comment icon -> comments section expands
   Type: "Amazing colors!"
   Click "Post"

   POST /api/posts/10/comments (X-User-Id: 1, X-Username: john_doe)
   Body: { text: "Amazing colors!" }

   Backend PostService.addComment():
   a. Find post 10
   b. Save: Comment { postId: 10, userId: 1, text: "Amazing colors!" }
   c. Send notification:
      sendNotification(1, 2, "COMMENT",
        "john_doe commented on your post: Amazing colors!", 10)

   Frontend: Comment appears at top of comments list
   commentsCount: 3 -> 4

4. USER B RECEIVES NOTIFICATIONS
   User B's Navbar polls every 30 seconds:

   GET /api/notifications/unread-count (X-User-Id: 2)
   -> { data: 2 }  // Two new notifications
   -> Red badge "2" appears on heart icon

   GET /api/notifications?page=0&size=10 (X-User-Id: 2)
   -> { data: { content: [
       { id: 20, type: "COMMENT", message: "john_doe commented on your post: Amazing colors!", read: false },
       { id: 19, type: "LIKE", message: "john_doe liked your post", read: false }
     ]}}

   User B clicks heart icon -> notification panel opens:
   [blue comment icon] john_doe commented on your post: Amazing colors!  -  1m
   [red heart icon] john_doe liked your post  -  2m

   User B clicks the like notification:
   PUT /api/notifications/19/read (X-User-Id: 2)
   -> Notification marked as read (blue background removed)
   -> Navigate to /profile/john_doe
```

### 17.3 Follow and Unfollow Flow

```
SETUP: User A (john_doe, id=1) visits User B's profile (jane_smith, id=2)

1. LOAD PROFILE PAGE
   Navigate to /profile/jane_smith

   Profile.tsx loads:
   a. GET /api/auth/profile/jane_smith
      -> { data: { id: 2, username: "jane_smith", fullName: "Jane Smith", bio: "Travel blogger" } }

   b. GET /api/posts/user/2 (with X-User-Id: 1)
      -> { data: [... list of jane's posts ...] }

   c. GET /api/follows/count/2
      -> { data: { followersCount: 150, followingCount: 89 } }

   d. GET /api/follows/check?followingId=2 (X-User-Id: 1)
      -> { data: false }  // Not following yet

   Display: "Follow" button (blue)

2. USER A CLICKS "FOLLOW"
   Optimistic update:
   - Button changes to "Following" (gray)
   - followersCount: 150 -> 151

   POST /api/follows (X-User-Id: 1, X-Username: john_doe)
   Body: { followingId: 2, followingUsername: "jane_smith" }

   Backend FollowService.followUser():
   a. Validate: 1 != 2 (not following self) ✓
   b. Check: existsByFollowerIdAndFollowingId(1, 2) -> false ✓
   c. Save: Follow { followerId: 1, followingId: 2 }
   d. fetchUsername(2) via WebClient -> "jane_smith"
   e. Create notification:
      notificationService.createNotification(
        senderId: 1, receiverId: 2, type: "FOLLOW",
        message: "john_doe started following you", referenceId: followId
      )

   Response: { data: { id: 5, followerUsername: "john_doe", followingUsername: "jane_smith" } }

3. USER B SEES NOTIFICATION
   GET /api/notifications/unread-count -> { data: 1 }
   [green person icon] john_doe started following you  -  now

4. USER A CLICKS "FOLLOWING" (UNFOLLOW)
   Optimistic update:
   - Button changes back to "Follow" (blue)
   - followersCount: 151 -> 150

   DELETE /api/follows/2 (X-User-Id: 1)

   Backend FollowService.unfollowUser():
   a. Find follow record where followerId=1, followingId=2
   b. Delete the record

   If API call fails: revert to "Following" and 151
```

---

## 18. Viva Questions and Answers

### Architecture Questions (Q1-Q10)

**Q1: What is microservices architecture and why did you use it?**

A: Microservices architecture is a software design approach where an application is built as a collection of small, independent services, each running in its own process and communicating via lightweight protocols like HTTP REST. Each service is focused on a single business capability.

We used it because:
- **Independent Deployment**: We can update the trending service without redeploying the auth service. In a monolith, any change requires redeploying everything.
- **Fault Isolation**: If the trending service crashes, users can still log in, create posts, and follow people. In a monolith, one crash takes down everything.
- **Independent Scaling**: If the post feed is getting heavy traffic, we can run 5 instances of post-service while keeping 1 instance of trending-service.
- **Technology Freedom**: Each service could use a different technology if needed. For example, trending-service could use Redis for caching without affecting other services.
- **Team Independence**: Different developers can work on different services simultaneously without merge conflicts.

**Q2: What is the role of the API Gateway?**

A: The API Gateway (Spring Cloud Gateway, port 8080) is the single entry point for all client requests. It has five key responsibilities:

1. **Routing**: Maps URL paths to backend services. For example, `/api/posts/**` goes to post-service, `/api/follows/**` goes to follow-service. Without the gateway, the frontend would need to know the address of every service.

2. **Authentication**: The JwtAuthenticationFilter validates JWT tokens for protected endpoints. It extracts the username and userId from the token and passes them as headers (X-Username, X-User-Id) to downstream services. This means only the gateway needs the JWT secret key.

3. **CORS Handling**: Configures Cross-Origin Resource Sharing to allow the frontend (localhost:3000) to call the backend (localhost:8080). Without this, browsers would block all API calls.

4. **Load Balancing**: When multiple instances of a service are running, the gateway distributes requests across them using round-robin. It uses Consul to discover available instances.

5. **Header Deduplication**: Prevents duplicate CORS headers when both the gateway and backend services add them, which would cause browsers to reject the response.

**Q3: How does Consul service discovery work?**

A: Consul is a service registry that enables services to find each other dynamically. Here is the complete flow:

**Registration**: When a service starts, Spring Cloud Consul auto-configuration registers it with Consul, providing the service name, IP address, port, and health check URL. For example, authentication-service registers as `{"name": "authentication-service", "address": "192.168.1.5", "port": 8081, "healthCheck": "/actuator/health"}`.

**Health Checking**: Every 10 seconds, Consul sends an HTTP GET request to each service's `/actuator/health` endpoint. If the response is HTTP 200 with `"status": "UP"`, the service is marked healthy (green in Consul UI). If it fails or times out, the service is marked unhealthy (red).

**Discovery**: When the API Gateway needs to route a request to `post-service`, it queries Consul: "Give me all healthy instances of post-service." Consul returns a list of IP:port combinations. Spring Cloud LoadBalancer picks one using round-robin.

**Deregistration**: When a service shuts down gracefully, it deregisters from Consul. If it crashes, Consul detects the failure through health checks and removes it from the registry.

**Q4: What is a Circuit Breaker? Explain with a real example from your project.**

A: A Circuit Breaker is a design pattern that prevents cascading failures in distributed systems. It works like an electrical circuit breaker: when too many failures occur, it "trips" and stops further calls.

**Real example**: When PostService creates a post, it calls trending-service to update hashtag counts. If trending-service goes down:

- **Without Circuit Breaker**: Every post creation waits 30 seconds for the trending-service call to timeout. If 100 users create posts simultaneously, PostService has 100 threads blocked waiting. Eventually PostService runs out of threads and stops responding. Now both trending-service AND post-service are down.

- **With Circuit Breaker** (our implementation):
  - First 5 calls to trending-service fail (within the sliding window of 10)
  - Circuit breaker detects 100% failure rate (exceeds 50% threshold)
  - Circuit OPENS: all further calls skip trending-service and use the fallback method
  - After 60 seconds, circuit moves to HALF-OPEN: allows 3 test calls
  - If trending-service is back, those 3 calls succeed, circuit CLOSES
  - Normal operation resumes

**Q5: Why did you use database-per-service instead of a shared database?**

A: We use four separate databases because:

- **Loose Coupling**: If we add a new column to the posts table, we only need to modify and redeploy post-service. With a shared database, other services might have code that depends on the old schema.

- **Independent Deployment**: Each service can run its own database migrations. We use `spring.jpa.hibernate.ddl-auto: update` which automatically updates the schema based on entity definitions.

- **Performance Isolation**: Heavy queries on the posts table (searching millions of posts) do not slow down authentication queries. Each database can be tuned independently.

- **Clear Ownership**: When debugging a notification issue, we know to look at `instagram_follows` database. There is no ambiguity about which service owns which data.

- **Technology Freedom**: In the future, we could move trending-service to use Redis (in-memory database) for faster hashtag lookups, without affecting other services.

**Tradeoff**: Cross-service data needs API calls. For example, to display a post with the author's username, post-service must call auth-service via WebClient, adding ~50ms latency. In a shared database, this would be a simple SQL JOIN.

**Q6: How does JWT authentication work end-to-end?**

A: JWT (JSON Web Token) authentication involves three phases:

**Phase 1 - Token Creation (Login)**:
1. User sends username "john_doe" and password "Test@1234"
2. AuthService finds the user in the database
3. BCrypt.matches() compares the plain password with the stored hash
4. If match, JwtUtil.generateToken() creates a token:
   - Header: `{"alg":"HS256"}` (algorithm)
   - Payload: `{"sub":"john_doe","userId":1,"iat":1700000000,"exp":1700086400}`
   - Signature: HMAC-SHA256 of header+payload using the secret key
5. Token is returned to the frontend as a string: `eyJhbGci...`

**Phase 2 - Token Storage (Frontend)**:
1. Frontend calls `localStorage.setItem("token", "eyJhbGci...")`
2. The Axios request interceptor reads this token before every API call
3. Adds header: `Authorization: Bearer eyJhbGci...`

**Phase 3 - Token Validation (Every Request)**:
1. API Gateway receives request with `Authorization: Bearer eyJhbGci...`
2. JwtAuthenticationFilter extracts the token (removes "Bearer " prefix)
3. Parses the token using the same secret key
4. Verifies: signature is valid AND expiration time has not passed
5. Extracts: username="john_doe", userId=1
6. Adds headers to downstream request: `X-Username: john_doe`, `X-User-Id: 1`
7. Backend service reads these headers directly

**Q7: Why BCrypt for password hashing?**

A: BCrypt is specifically designed for password hashing, unlike general-purpose hash functions like SHA-256:

1. **Automatic Salt**: Each BCrypt call generates a random 16-byte salt. So `BCrypt("password")` produces a different hash every time. This prevents rainbow table attacks where attackers pre-compute hashes.

2. **Configurable Cost Factor**: BCrypt uses a cost factor (default 10 in Spring = 2^10 = 1024 rounds). This makes hashing deliberately slow (~100ms per hash). While 100ms is acceptable for a login, an attacker trying millions of passwords would need millions of 100ms operations.

3. **Industry Standard**: OWASP (Open Web Application Security Project) recommends BCrypt for password storage. Spring Security provides `BCryptPasswordEncoder` out of the box.

**Example**:
```
Input: "Test@1234"
Hash:  "$2a$10$iL2gXkoS0B42ESpOyQEkFuak4mU.9yMrk7Hu1VS5sa1qhhv4Rxewm"
       ^^^^  ^^
       algo  cost (2^10 rounds)
```

**Q8: What happens when a JWT token expires?**

A: Our tokens expire after 24 hours. When a token expires:

1. **Backend**: The API Gateway's JwtAuthenticationFilter tries to parse the token. `Jwts.parser().parseSignedClaims(token)` throws `ExpiredJwtException`. The filter catches this and returns HTTP 401 Unauthorized.

2. **Frontend**: The Axios response interceptor catches the 401 error:
   ```typescript
   if (error.response?.status === 401) {
       localStorage.removeItem('token');     // Clear expired token
       window.location.href = '/login';       // Force redirect to login
   }
   ```

3. **User**: The user is redirected to the login page and must enter credentials again to get a new token.

**Q9: How does the frontend manage authentication state?**

A: We use React Context API with the AuthContext pattern:

1. **AuthProvider** wraps the entire app in App.tsx
2. On app load, it reads the token from `localStorage`
3. If a token exists, it calls `refreshProfile()` which hits `GET /api/auth/profile`
4. The response sets the `user` state with the full user object
5. `isAuthenticated` is derived: `!!token` (true if token is not null)
6. Any component can call `useAuth()` to get user, token, login(), logout()
7. `ProtectedRoute` checks `isAuthenticated` and redirects to `/login` if false

**Q10: What is CORS and why is it needed?**

A: CORS (Cross-Origin Resource Sharing) is a browser security mechanism. It blocks web pages from making HTTP requests to a different domain than the one that served the page.

In our project:
- Frontend is served from `http://localhost:3000`
- API Gateway runs on `http://localhost:8080`
- These are different "origins" (different ports)

Without CORS configuration, the browser would block ALL API calls from the frontend. Our gateway allows this by responding with:
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

---

### Frontend Questions (Q11-Q20)

**Q11: How does the Axios interceptor work?**

A: Axios interceptors are middleware functions that run before every request and after every response:

**Request Interceptor**: Before sending any HTTP request, the interceptor reads the JWT token from localStorage and adds it as an Authorization header. This means no component needs to manually add the token.

**Response Interceptor**: After receiving any HTTP response, if the status is 401 (token expired/invalid), the interceptor removes the token and redirects to login. This provides centralized error handling.

**Q12: How do image filters work without external libraries?**

A: We use native CSS `filter` property for preview and HTML5 Canvas API for applying filters permanently:

1. 12 presets are defined as JavaScript objects with values for brightness, contrast, saturation, sepia, grayscale, and hue-rotate
2. Preview: `<img style="filter: brightness(110%) contrast(120%) saturate(130%)" />`
3. Apply: Canvas `ctx.filter` property accepts the same CSS filter string
4. Canvas draws the filtered image and exports it as a JPEG base64 data URL
5. No npm packages needed - works in all modern browsers

**Q13: What is optimistic UI update?**

A: It means updating the UI immediately before the server responds, then reverting if the server call fails. We use this for likes:

1. User clicks heart -> UI immediately shows red heart and incremented count
2. API call is made in the background
3. If successful: nothing changes (UI is already correct)
4. If failed: UI reverts to previous state (gray heart, original count)

This makes the app feel instant instead of waiting 200-500ms for the server response.

**Q14: How does the responsive design work?**

A: We use CSS media queries with three breakpoints:

- **Mobile (<768px)**: Sidebar is completely hidden. A fixed bottom navigation bar appears with 5 icons (Home, Search, Create, Explore, Profile). Main content takes full width.
- **Tablet (768px-1263px)**: Sidebar collapses to 72px showing only icons. Logo changes from text "Instagram" to the camera icon. Search and notification panels slide out from the left.
- **Desktop (>=1264px)**: Full 245px sidebar with icons and text labels.

**Q15: How does notification polling work?**

A: The Navbar component uses `setInterval` to poll for notifications every 30 seconds. On each poll, it makes two parallel API calls: one for the recent notifications list and one for the unread count. The unread count shows as a red badge on the heart icon. When the user clicks a notification, it is marked as read and the user is navigated to the relevant profile.

**Q16: How does the ProtectedRoute component work?**

A: ProtectedRoute is a wrapper component that checks authentication:
```typescript
const ProtectedRoute = ({ children }) => {
    const { isAuthenticated } = useAuth();
    return isAuthenticated ? children : <Navigate to="/login" />;
};
```
If the user is not authenticated, they are redirected to the login page. If they are, the child component renders normally.

**Q17: How does IntersectionObserver track post views?**

A: Each PostCard creates an IntersectionObserver that watches when the post's DOM element becomes 50% visible in the viewport. When triggered, it calls `POST /api/posts/{id}/view`. A React ref (`viewRecorded`) prevents recording the same view multiple times per component mount. The backend has its own 5-minute cooldown per user per post.

**Q18: Why TypeScript instead of JavaScript?**

A: TypeScript adds static typing which catches bugs at compile time. For example, if an API response changes from `data.username` to `data.user_name`, TypeScript shows an error immediately instead of failing at runtime. It also provides better IDE autocomplete and makes code self-documenting through interface definitions.

**Q19: How does the search with debounce work?**

A: When the user types in the search bar, we don't make an API call on every keystroke. Instead, we use debouncing:
1. User types "jo" -> start 300ms timer
2. User types "joh" -> cancel previous timer, start new 300ms timer
3. User types "john" -> cancel previous timer, start new 300ms timer
4. User stops typing -> 300ms passes -> API call: `GET /api/auth/users/search?query=john`

This reduces API calls from ~20 (one per keystroke) to ~1-2.

**Q20: How does the dark theme work?**

A: The dark theme is enforced through multiple layers:
1. `html { color-scheme: dark; }` tells browsers to use dark mode defaults for form elements
2. `body { background: #000; color: #f5f5f5; }` sets the base colors
3. Every container class has explicit `background: #000` to prevent white flashes
4. All form elements (inputs, selects, textareas) have explicit dark background (#121212) and light text (#f5f5f5) colors
5. All borders use dark gray (#262626, #363636) instead of light colors

---

### Backend Questions (Q21-Q30)

**Q21: How do services communicate with each other?**

A: Services use Spring WebFlux WebClient with @LoadBalanced annotation for HTTP-based inter-service communication. The @LoadBalanced annotation integrates with Spring Cloud LoadBalancer, which uses Consul for service discovery. When PostService calls `http://authentication-service/api/auth/users/1`, the LoadBalancer resolves "authentication-service" to an actual IP:port using Consul, picks one instance via round-robin, and sends the HTTP request.

**Q22: What is @LoadBalanced and why is it needed?**

A: @LoadBalanced is a Spring Cloud annotation that wraps the WebClient with service discovery and load balancing capabilities. Without it, `http://authentication-service` would be treated as a literal hostname (which does not exist). With it, Spring Cloud intercepts the URL, queries Consul for instances of "authentication-service", and replaces the service name with an actual IP:port.

**Q23: How does Spring Data JPA generate SQL queries?**

A: Spring Data JPA uses method name conventions to generate SQL automatically:

| Method Name | Generated SQL |
|------------|---------------|
| `findByUsername(String)` | `SELECT * FROM users WHERE username = ?` |
| `findByUserIdOrderByCreatedAtDesc(Long)` | `SELECT * FROM posts WHERE user_id = ? ORDER BY created_at DESC` |
| `existsByPostIdAndUserId(Long, Long)` | `SELECT EXISTS(SELECT 1 FROM post_likes WHERE post_id = ? AND user_id = ?)` |
| `countByPostId(Long)` | `SELECT COUNT(*) FROM post_comments WHERE post_id = ?` |
| `findByUsernameContainingIgnoreCase(String)` | `SELECT * FROM users WHERE LOWER(username) LIKE LOWER('%?%')` |

**Q24: Why are notifications in the follow-service?**

A: Notifications are in the follow-service because the FOLLOW notification is the most common and is already part of the follow domain. The follow-service already has the social relationship data needed for notifications. Other services send notifications via simple WebClient HTTP calls. If the notification system grows complex (WebSocket, email, push), it could be extracted into its own service.

**Q25: How does the feed pagination work?**

A: The frontend requests pages of posts: `GET /api/posts/public?page=0&size=20`. Spring Data JPA uses `PageRequest.of(page, size)` to create a `Pageable` object. The repository method returns a `Page<Post>` object containing the content array, total elements, total pages, current page number, and page size. The frontend shows a "Load More" button that increments the page number.

**Q26: How does @Transactional work?**

A: @Transactional ensures that a method runs within a database transaction. If any step fails, all database changes are rolled back. For example, in `deletePost()`, we delete comments, likes, and the post itself. If deleting comments succeeds but deleting the post fails, @Transactional rolls back the comment deletion too, keeping the database consistent.

**Q27: What is ModelMapper and why use it?**

A: ModelMapper automatically maps fields from one object to another by matching field names. For example, it maps `User` entity (with password, passwordResetToken) to `UserProfileDto` (without sensitive fields). This prevents exposing internal fields to the API and reduces boilerplate code.

**Q28: How does the API response format work?**

A: All endpoints return a consistent `ApiResponse` format with fields: `message` (description), `success` (boolean), `timestamp` (ISO date), and `data` (the actual payload). This makes error handling predictable on the frontend - every response has the same structure whether it succeeds or fails.

**Q29: What is the difference between public and protected routes?**

A: Public routes do not require JWT authentication. Examples: register, login, view public feed, view trending hashtags. These are accessible to anyone. Protected routes require a valid JWT token in the Authorization header. Examples: create post, like, comment, follow. The API Gateway's JwtAuthenticationFilter is applied only to protected routes.

**Q30: How does the media upload work?**

A: The frontend uploads a file using `multipart/form-data` to `POST /api/media/upload`. Post-service receives the file, reads the bytes, and stores them in the `media_files` table as a MEDIUMBLOB (up to 16MB). It returns a media URL like `/api/media/5`. When displaying the post, the frontend requests `GET /api/media/5`, and post-service reads the blob from the database and returns it with the correct Content-Type header.

---

### Security Questions (Q31-Q35)

**Q31: How is the application secured against common attacks?**

A: Multiple security measures are in place:
- **Authentication**: JWT tokens with 24-hour expiry and HMAC-SHA256 signing
- **Password Security**: BCrypt hashing with automatic salting (raw password never stored)
- **CORS**: Only allows requests from specific frontend origins
- **CSRF**: Disabled because we use stateless JWT (no cookies/sessions)
- **SQL Injection**: Prevented by JPA parameterized queries (never raw SQL concatenation)
- **Input Validation**: Jakarta Bean Validation (`@NotNull`, `@Size`) on all request DTOs
- **Authorization**: API Gateway validates tokens before forwarding; services check ownership
- **Account Lockout**: Frontend implements 3-attempt lockout with 60-second timer

**Q32: Why is CSRF disabled?**

A: CSRF attacks exploit session cookies that browsers automatically include. Our app uses JWT tokens in the Authorization header, which browsers never automatically include. Therefore CSRF is impossible and CSRF protection is not needed.

**Q33: What is stateless authentication?**

A: In stateless authentication, the server does not store any session information. Each request carries its own authentication (the JWT token). This means any server instance can handle any request, making horizontal scaling easy.

**Q34: How is authorization handled?**

A: Authorization is handled at two levels:
1. **Gateway level**: Protected routes require a valid JWT token
2. **Service level**: Services check ownership. For example, `deletePost()` checks `post.getUserId().equals(userId)` to ensure users can only delete their own posts.

**Q35: What would you do differently for production security?**

A: For production, we would add: HTTPS/TLS encryption, move JWT secret to a vault (like HashiCorp Vault), add refresh tokens (short-lived access + long-lived refresh), implement rate limiting, add API key authentication for inter-service calls, use HTTPS for inter-service communication, and add audit logging.

---

### Database Questions (Q36-Q40)

**Q36: What is the InnoDB engine?**

A: InnoDB is MySQL's default storage engine. It provides ACID transactions (Atomicity, Consistency, Isolation, Durability), row-level locking (multiple users can modify different rows simultaneously), foreign key support, and crash recovery via redo logs.

**Q37: What are indexes and why are they important?**

A: An index is a data structure that speeds up queries. Without an index on `user_id` in the `posts` table, MySQL would scan every row to find posts by a specific user (full table scan). With the index, it uses a B-tree to find matching rows in O(log n) time. Our schema has indexes on all foreign keys and commonly queried columns.

**Q38: What is a unique constraint?**

A: A unique constraint ensures no two rows have the same value in specified columns. We use `UNIQUE KEY uk_post_user (post_id, user_id)` on `post_likes` to prevent a user from liking the same post twice. If they try, MySQL returns an error.

**Q39: What does ON DELETE CASCADE do?**

A: When a post is deleted, `ON DELETE CASCADE` automatically deletes all related rows in child tables (post_likes, post_comments, post_hashtags, video_views). Without it, we would get foreign key constraint errors or orphan records.

**Q40: Why utf8mb4 charset?**

A: `utf8mb4` supports all Unicode characters including emojis. Standard `utf8` in MySQL only supports 3-byte characters. Since social media posts frequently contain emojis, `utf8mb4` ensures they are stored correctly.

---

### DevOps Questions (Q41-Q45)

**Q41: How does Docker Compose orchestrate the services?**

A: Docker Compose reads `docker-compose.yml` and manages all containers. It handles startup order using `depends_on` with conditions (service_healthy for MySQL, service_started for Consul). It provides networking (containers communicate via service names), port mapping (container ports to host ports), and volume management (mysql-data for persistence).

**Q42: How would you scale this application?**

A: Multiple approaches:
- **Horizontal scaling**: Run multiple instances of a service. Consul discovers all instances and the load balancer distributes requests.
- **Database read replicas**: Route read queries to replica databases.
- **Caching**: Add Redis for frequently accessed data like trending hashtags and user profiles.
- **Message queue**: Replace synchronous WebClient calls with Kafka/RabbitMQ for notifications.
- **CDN**: Serve media files through a CDN instead of the post-service.
- **Kubernetes**: Replace Docker Compose with Kubernetes for production-grade orchestration with auto-scaling.

**Q43: What is the difference between Docker and Docker Compose?**

A: Docker runs individual containers. Docker Compose orchestrates multiple containers defined in a single YAML file, managing their startup order, networking, and shared volumes.

**Q44: How do services communicate inside Docker?**

A: Docker Compose creates a shared network. Services communicate using their service names as hostnames (e.g., `mysql`, `consul`). These resolve to the container's internal IP addresses.

**Q45: What is a Docker volume?**

A: A volume is persistent storage managed by Docker. Our `mysql-data` volume stores MySQL data files on the host disk. Without it, all data would be lost when the container stops.

---

### General Questions (Q46-Q50)

**Q46: What challenges did you face?**

A: Key challenges included: CORS configuration (duplicate headers), health check failures (missing actuator dependency), username resolution across services (adding latency), and frontend dependency issues (replaced react-easy-crop with pure CSS filters).

**Q47: What would you improve with more time?**

A: Real-time notifications (WebSocket), message queues (Kafka), caching (Redis), cloud storage (S3), email for password reset, direct messaging, rate limiting, monitoring (Prometheus/Grafana), and comprehensive testing.

**Q48: How does this project demonstrate distributed systems knowledge?**

A: It demonstrates: service decomposition, service discovery, API gateway pattern, circuit breaker, database per service, load balancing, inter-service communication, health monitoring, containerization, and stateless authentication.

**Q49: What is the difference between monolithic and microservices?**

A: A monolith is a single deployable unit containing all features. Microservices split features into independent services. Monoliths are simpler to develop initially but harder to scale and maintain. Microservices add complexity but provide independent deployment, scaling, and fault isolation.

**Q50: Can you explain the complete flow when a user opens the app for the first time?**

A:
1. Browser loads `http://localhost:3000` -> React app loads
2. AuthProvider checks localStorage -> no token found
3. User visits `/` -> ProtectedRoute checks isAuthenticated -> false
4. Redirect to `/login` -> Login page renders
5. User enters credentials and clicks Login
6. `POST /api/auth/login` -> API Gateway -> auth-service
7. BCrypt validates password -> JWT token generated -> returned to frontend
8. Frontend stores token in localStorage, fetches profile
9. isAuthenticated becomes true -> redirect to `/`
10. HomeFeed loads -> `GET /api/posts/public?page=0&size=20`
11. Posts rendered with PostCard components
12. Navbar starts polling notifications every 30 seconds
13. User can now create posts, like, comment, follow, search, and explore trending content

---

*This report was prepared for the ZKinsta Instagram Clone Microservices Project.*
*Repository: https://github.com/pradhumangit341512/zkinsta*
*Total report length: ~2000+ lines covering all aspects of the project*
