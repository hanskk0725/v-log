# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

- **프로젝트명**: v-log
- **설명**: Q&A 기반 커뮤니티 서비스
- **개발 기간**: 2025.12.22 ~ 2026.01.07
- **팀원**: 백종현(팀장), 구창현, 김한성, 박혜은, 박예진

## 빌드 및 실행

```bash
./gradlew build        # 빌드
./gradlew bootRun      # 실행
./gradlew test         # 테스트
./gradlew clean build  # 클린 빌드

# Docker로 MySQL 실행
docker-compose up -d   # MySQL 컨테이너 시작 (port 13306)
```

## 기술 스택

- **Frontend**: Next.js (예정)
- **Backend**: Spring Boot 3.5.9 / Java 21
- **Database**: Spring Data JPA + MySQL (port 13306, database: vlog)
- **Security**: Spring Security (세션 기반 인증)
- **Validation**: Jakarta Validation (@Valid, @NotBlank, @Email)
- **Build**: Gradle
- **DevOps**: Docker, Git, GitHub

## 패키지 구조

```
com.likelion.vlog
├── config/
│   └── ProjectSecurityConfig.java    # Spring Security 설정 (세션 기반, CORS)
├── controller/
│   ├── PostController.java           # 게시글 CRUD API (/api/v1/posts)
│   ├── AuthController.java           # 인증 API (/auth)
│   └── UserController.java           # 사용자 API (/users)
├── service/
│   ├── PostService.java              # 게시글 비즈니스 로직
│   ├── AuthService.java              # 인증 로직 (UserDetailsService 구현)
│   └── UserService.java              # 사용자 비즈니스 로직
├── repository/
│   ├── PostRepository.java           # 게시글 (페이징/필터링 JPQL)
│   ├── TagRepository.java            # 태그 조회
│   ├── TagMapRepository.java         # @Modifying 벌크 삭제
│   ├── UserRepository.java           # 사용자 조회
│   ├── BlogRepository.java           # 블로그 조회
│   ├── LikeRepository.java           # Sprint 2 대기
│   └── CommentRepository.java        # Sprint 2 대기
├── dto/
│   ├── request/
│   │   ├── PostCreateRequest.java    # 게시글 생성 요청
│   │   └── PostUpdateRequest.java    # 게시글 수정 요청
│   ├── response/
│   │   ├── PostResponse.java         # 상세 조회
│   │   ├── PostListResponse.java     # 목록 조회 (summary 100자)
│   │   ├── CommentResponse.java      # Sprint 2 대기
│   │   ├── AuthorResponse.java       # 작성자 정보
│   │   └── PageResponse.java         # 제네릭 페이징 래퍼
│   ├── auth/
│   │   ├── SignupRequestDto.java     # 회원가입 요청
│   │   └── LoginRequestDto.java      # 로그인 요청
│   ├── user/
│   │   ├── UserDto.java              # 사용자 정보 응답
│   │   └── UserUpdateRequestDto.java # 사용자 수정 요청
│   └── common/
│       └── ApiResponse.java          # 통용 API 응답
├── exception/
│   ├── NotFoundException.java        # 404 - 정적 팩토리 메서드
│   ├── ForbiddenException.java       # 403 - 권한 없음
│   ├── DuplicateException.java       # 409 - 중복 데이터
│   ├── GlobalExceptionHandler.java   # @RestControllerAdvice
│   └── AuthEntryPoint.java           # 인증 실패 처리 (401)
└── entity/
    ├── BaseEntity.java               # createdAt/updatedAt (JPA Auditing)
    ├── User.java                     # of() 팩토리 메서드
    ├── Blog.java                     # User와 1:1 관계
    ├── Post.java                     # create(), update() 메서드
    ├── Comment.java                  # create(), createReply(), update()
    ├── Tag.java                      # create() 메서드
    ├── TagMap.java                   # create() 메서드
    ├── Like.java                     # create() 메서드
    └── Follow.java                   # create() 메서드
```

## API 엔드포인트

### 게시글 API (`/api/v1/posts`)

| Method | Endpoint | 설명 | 인증 | 쿼리 파라미터 |
|--------|----------|------|------|--------------|
| GET | `/api/v1/posts` | 목록 조회 | X | `page`, `size`, `tag`, `blogId` |
| GET | `/api/v1/posts/{id}` | 상세 조회 | X | - |
| POST | `/api/v1/posts` | 작성 | O | - |
| PUT | `/api/v1/posts/{id}` | 수정 | O (작성자만) | - |
| DELETE | `/api/v1/posts/{id}` | 삭제 | O (작성자만) | - |

### 인증 API (`/auth`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/auth/signup` | 회원가입 (Blog 자동 생성) | X |
| POST | `/auth/login` | 로그인 (세션 저장) | X |
| POST | `/auth/logout` | 로그아웃 (세션 무효화) | O |

### 사용자 API (`/users`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/users/{id}` | 사용자 정보 조회 | X |
| PUT | `/users/{id}` | 사용자 정보 수정 | O |
| DELETE | `/users/{id}` | 회원 탈퇴 | O |

## Entity 관계도

```
User (1) ──────── (1) Blog (1) ──────── (*) Post
  │                                       │
  │                                       ├── (*) TagMap (*) ── (1) Tag
  │                                       │
  │                                       ├── (*) Comment (self-ref: 대댓글)
  │                                       │
  └── (*) Like ── (*) ────────────────────┘
```

## 정적 팩토리 메서드

### Entity 생성
```java
// 사용자 (비밀번호 암호화 포함)
User.of(SignupRequestDto dto, PasswordEncoder encoder)

// 게시글
Post.create(String title, String content, Blog blog)
post.update(String title, String content)

// 태그
Tag.create(String title)
TagMap.create(Post post, Tag tag)

// 댓글
Comment.create(User user, Post post, String content)
Comment.createReply(User user, Post post, Comment parent, String content)
comment.update(String content)

// 좋아요/팔로우
Like.create(User user, Post post)
Follow.create(User follower, User following)
```

### 커스텀 예외
```java
NotFoundException.post(Long postId)      // "게시글을 찾을 수 없습니다. id=1"
NotFoundException.user(Long userId)      // "사용자를 찾을 수 없습니다. id=1"
NotFoundException.user(String email)     // "사용자를 찾을 수 없습니다. email=xxx"
NotFoundException.blog(Long userId)      // "블로그를 찾을 수 없습니다. userId=1"
ForbiddenException.postUpdate()          // "게시글 수정 권한이 없습니다."
ForbiddenException.postDelete()          // "게시글 삭제 권한이 없습니다."
DuplicateException.email(String email)   // "이미 존재하는 이메일입니다."
```

## 응답 형식

### 성공 응답
```json
// 게시글 상세 (PostResponse)
{
  "postId": 1,
  "title": "제목",
  "content": "내용",
  "author": { "userId": 1, "nickname": "작성자" },
  "tags": ["Spring", "JPA"],
  "createdAt": "2025-12-24T12:00:00",
  "updatedAt": "2025-12-24T12:00:00"
}

// 페이징 (PageResponse)
{
  "content": [...],
  "pageInfo": {
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```

### 에러 응답
```json
// GlobalExceptionHandler (404, 403, 409, 400, 500)
{
  "status": 404,
  "error": "Not Found",
  "message": "게시글을 찾을 수 없습니다. id=999",
  "timestamp": "2025-12-24T12:00:00"
}

// AuthEntryPoint (401)
{
  "message": "인증되지 않았습니다.",
  "status": "401",
  "path": "/api/v1/posts",
  "timestamp": "2025-12-24T12:00:00"
}
```

## 주요 설계 패턴

### Post 삭제 시 연관 데이터 처리
```java
// PostService.deletePost()
tagMapRepository.deleteAllByPost(post);    // 태그맵 삭제
postRepository.delete(post);               // 게시글 삭제
```

### 트랜잭션 설정
```java
@Service
@Transactional(readOnly = true)  // 클래스 기본값 (읽기 전용)
public class PostService {

    @Transactional  // 쓰기 메서드만 오버라이드
    public PostResponse createPost(...) { }
}
```

## 코딩 컨벤션

### Entity
- `BaseEntity` 상속 필수 (`createdAt`/`updatedAt` JPA Auditing)
- `@Setter` 사용 금지 → 비즈니스 메서드 사용 (`post.update()`)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - JPA 프록시용
- `FetchType.LAZY` 전역 적용

### Repository
- 벌크 연산: `@Modifying` + `@Query` 필수

### Service
- 커스텀 예외만 사용 (`NotFoundException`, `ForbiddenException`, `DuplicateException`)
- `IllegalArgumentException` 사용 금지

### Controller
- 인증 정보: `@AuthenticationPrincipal UserDetails`
- 이메일 조회: `userDetails.getUsername()`
- HTTP 상태: 200 OK, 201 Created, 204 No Content

## 구현 현황

### 완료
- [x] 회원가입 / 로그인 / 로그아웃 (AuthController)
- [x] 게시글 CRUD (PostController)
- [x] 사용자 조회/수정/탈퇴 (UserController)
- [x] 해시태그 분류 (Tag, TagMap)
- [x] 세션 기반 인증 (Spring Security)
- [x] 패키지 구조 정리 (entity/entity → entity)

### 엔티티만 존재 (API 미구현)
- [ ] 좋아요 기능 (Like 엔티티 O, Controller X)
- [ ] 댓글/대댓글 기능 (Comment 엔티티 O, Controller X)
- [ ] 팔로우 기능 (Follow 엔티티 O, Controller X)

### 미구현
- [ ] 팔로우 게시글 알람 기능
- [ ] Frontend (Next.js)

---

## 남은 작업 (Sprint 2~3)

| 우선순위 | 기능 | 필요 파일 |
|---------|------|----------|
| 1 | 댓글 CRUD API | CommentController, CommentService |
| 2 | 좋아요 토글 API | LikeController, LikeService |
| 3 | 팔로우 기능 API | FollowController, FollowService |
| 4 | 팔로우 알람 | Notification 엔티티/서비스 |
| 5 | Frontend (Next.js) | 별도 프로젝트 |

---

## 코드 개선 (TODO)

### 우선순위 1 (Critical)
- [ ] AuthService: `IllegalArgumentException` → `DuplicateException.email()` 변경
- [ ] UserService: `IllegalArgumentException` → `NotFoundException.user()` 변경 (3곳)
- [ ] GlobalExceptionHandler: `DuplicateException` 핸들러 추가 (409 응답)
- [ ] AuthEntryPoint: 포맷 문자열 `%S` → `%s` 수정
- [ ] UserController: 권한 검증 추가 (본인만 수정/삭제 가능)

### 우선순위 2 (Major)
- [ ] User.java: `BaseEntity` 상속, `@Setter` 제거
- [ ] Blog.java: `@Setter` 제거
- [ ] PostController: 정렬 기준 변경 (`id` → `createdAt`)
- [ ] AuthService: `UsernameNotFoundException` → `NotFoundException.user(email)` 변경

### 우선순위 3 (Minor)
- [ ] AuthController: 중복 import 정리 (라인 21-22)
- [ ] PostService: 태그 조회 N+1 문제 (getTagNames() 개별 쿼리)
- [ ] 대댓글 조회 (CommentResponse에 children 추가)
- [ ] 운영 환경 프로파일 분리 (`application-prod.yaml`)
- [ ] `ddl-auto: update` → `validate` 변경 (운영)

## 코드 리뷰 히스토리

### 2025-12-25 코드 리뷰
- 전체 코드베이스 리뷰 완료
- Critical/Major/Minor 개선사항 분류
- CLAUDE.md 최신화

### 2025-12-25 리팩토링
- 패키지 구조 변경 (`entity/entity` → `entity`)
- PostController: `CustomUserDetails` → `UserDetails` 변경
- PostService: `userId` → `email` 파라미터 변경
- NotFoundException: `user(String email)` 메서드 추가

### 2025-12-24 Sprint 1 완료
- AuthController 추가 (회원가입/로그인/로그아웃)
- AuthService 추가 (UserDetailsService 구현)
- UserController, UserService 추가
- AuthEntryPoint 추가 (401 응답)
- DuplicateException 추가 (409 Conflict)
- PostResponse에서 likeCount, isLiked, comments 제거 (Sprint 2로 분리)

### 2025-12-23 리팩토링
- TagMapRepository @Modifying 추가
- 커스텀 예외 생성 (NotFoundException, ForbiddenException)
- GlobalExceptionHandler 추가
- Entity @Setter 제거, 비즈니스 메서드 추가
- BaseEntity 추출 (JPA Auditing)