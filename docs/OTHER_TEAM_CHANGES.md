# 다른 팀원에게 전달할 수정 사항

## 작성자: [댓글 기능 담당자]
## 작성일: 2025-12-29
## 최종 수정일: 2025-12-30

---

## 0. API 경로 참고사항 (중요)

현재 프로젝트의 API 경로와 `docs/API.md` 문서 사이에 차이가 있습니다:

| 구분 | 경로 |
|------|------|
| API.md 문서 | `/posts`, `/users` |
| 실제 구현 (PostController) | `/api/v1/posts` |
| 실제 구현 (UserController) | `/users` |

댓글 기능은 **실제 구현 기준**(`/api/v1/posts/{postId}/comments`)으로 작업합니다.
다른 담당자분들도 실제 코드 기준으로 작업해주세요.

---

## 1. PostController - ApiResponse 래핑 적용 (필수)

**현재 상태:**
```java
@GetMapping("/{postId}")
public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
    PostResponse response = postService.getPost(postId);
    return ResponseEntity.ok(response);
}
```

**변경 필요:**
```java
@GetMapping("/{postId}")
public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
    PostResponse response = postService.getPost(postId);
    return ResponseEntity.ok(ApiResponse.success("게시글 조회 성공", response));
}
```

**영향 받는 메서드:**
- `getPosts()` - 목록 조회
- `getPost()` - 상세 조회
- `createPost()` - 작성
- `updatePost()` - 수정
- `deletePost()` - 삭제 (204 No Content는 그대로 유지 가능)

---

## 2. 좋아요 기능 구현 시 게시글 응답 필드 추가 (좋아요 담당자)

`GET /api/v1/posts/{postId}` 응답에 다음 필드 추가 필요:

```java
// PostResponse.java에 추가
private int likeCount;        // 좋아요 수
private boolean isLiked;      // 현재 사용자의 좋아요 여부
```

**API 스펙 참고:**
```json
{
  "postId": 1,
  "title": "Spring Boot 시작하기",
  "content": "...",
  "author": { ... },
  "tags": ["Spring", "Java"],
  "likeCount": 15,
  "isLiked": false,
  "comments": [ ... ],
  "createdAt": "2024-12-23T10:00:00"
}
```

---

## 3. DTO 네이밍 통일 (선택, 권장)

새 컨벤션에 맞게 기존 DTO 이름 변경 권장:

| 기존 | 변경 권장 |
|------|----------|
| `SignupRequestDto` | `SignupPostRequest` |
| `LoginRequestDto` | `LoginPostRequest` |
| `UserUpdateRequestDto` | `UserUpdatePutRequest` |
| `UserDto` | `UserGetResponse` |

> 참고: auth 도메인은 HTTP 메서드 생략 허용 가능 (기존 유지해도 됨)

---

## 4. 컨벤션 정리

### API 응답 형식
```java
// 성공 (데이터 있음)
return ResponseEntity.ok(ApiResponse.success("메시지", data));

// 성공 (데이터 없음)
return ResponseEntity.ok(ApiResponse.success("메시지"));

// 생성 성공
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponse.success("생성 성공", data));

// 삭제 성공
return ResponseEntity.noContent().build();
```

### DTO 클래스 상단 주석
```java
/**
 * POST /auth/login 요청 객체
 */
public class LoginPostRequest {
    ...
}
```

---

## 5. LikeService - 커스텀 예외 적용 (필수, 좋아요 담당자)

**현재 상태:**
```java
// LikeService.java
if (user == null) {
    throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
}
if (post == null) {
    throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
}
```

**변경 필요:**
```java
// LikeService.java
User user = userRepository.findByEmail(email)
    .orElseThrow(() -> NotFoundException.user(email));

Post post = postRepository.findById(postId)
    .orElseThrow(() -> NotFoundException.post(postId));
```

**이유:** 프로젝트 컨벤션에 따라 커스텀 예외(`NotFoundException`, `ForbiddenException`)만 사용

---

## 6. AuthService - 커스텀 예외 적용 (필수, 인증 담당자)

**현재 상태:**
```java
// AuthService.java - signup 메서드
if (userRepository.existsByEmail(email)) {
    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
}
```

**변경 필요:**
```java
// AuthService.java
if (userRepository.existsByEmail(email)) {
    throw DuplicateException.email(email);
}
```

**DuplicateException 클래스 (이미 존재하면 사용, 없으면 생성):**
```java
// exception/DuplicateException.java
public class DuplicateException extends RuntimeException {
    public DuplicateException(String message) {
        super(message);
    }

    public static DuplicateException email(String email) {
        return new DuplicateException("이미 존재하는 이메일입니다: " + email);
    }
}
```

---

## 7. UserService - 커스텀 예외 적용 (필수, 사용자 담당자)

**현재 상태:**
```java
// UserService.java
if (user == null) {
    throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
}

// 권한 검증
if (!user.getId().equals(currentUserId)) {
    throw new IllegalArgumentException("권한이 없습니다.");
}
```

**변경 필요:**
```java
// UserService.java
User user = userRepository.findById(userId)
    .orElseThrow(() -> NotFoundException.user(userId));

// 권한 검증
if (!user.getId().equals(currentUserId)) {
    throw new ForbiddenException("사용자 정보 수정 권한이 없습니다.");
}
```

---

## 8. UserController - API 경로 수정 (필수, 사용자 담당자)

**현재 상태:**
```java
@RestController
@RequestMapping("/api/v1/tags/users")  // 잘못된 경로
public class UserController {
    ...
}
```

**변경 필요:**
```java
@RestController
@RequestMapping("/api/v1/users")  // 올바른 경로
public class UserController {
    ...
}
```

**이유:** `/api/v1/tags/users`는 태그 관련 경로로 오인될 수 있음

---

## 우선순위 정리

| 우선순위 | 항목 | 담당자 | 상태 |
|---------|------|--------|------|
| 🔴 Critical | PostController ApiResponse 래핑 | 게시글 담당 | ✅ |
| 🔴 Critical | LikeService 커스텀 예외 | 좋아요 담당 | ⬜ |
| 🔴 Critical | AuthService 커스텀 예외 | 인증 담당 | ⬜ |
| 🔴 Critical | UserService 커스텀 예외 | 사용자 담당 | ⬜ |
| 🔴 Critical | UserController 경로 수정 | 사용자 담당 | ⬜ |
| 🟡 권장 | PostResponse likeCount/isLiked 추가 | 좋아요 담당 | ⬜ |
| 🟢 선택 | DTO 네이밍 통일 | 전체 | ⬜ |

---

## 질문/문의

댓글 기능 구현 관련 질문은 [담당자]에게 연락 부탁드립니다.
