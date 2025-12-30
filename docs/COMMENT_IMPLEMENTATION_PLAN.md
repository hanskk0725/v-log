# 댓글/대댓글 기능 구현 계획서

## 담당자: [본인 이름]
## 작성일: 2025-12-29
## 최종 수정일: 2025-12-30

---

## 구현 범위

### 엔드포인트
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/posts/{postId}/comments` | 댓글 작성 | O |
| PUT | `/api/v1/posts/{postId}/comments/{commentId}` | 댓글 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{postId}/comments/{commentId}` | 댓글 삭제 | O (작성자) |
| POST | `/api/v1/posts/{postId}/comments/{commentId}/replies` | 답글 작성 | O |
| PUT | `/api/v1/posts/{postId}/comments/{commentId}/replies/{replyId}` | 답글 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{postId}/comments/{commentId}/replies/{replyId}` | 답글 삭제 | O (작성자) |

> **참고**: 현재 PostController가 `/api/v1/posts`를 사용하므로 동일한 prefix 유지

### 추가 작업
- 게시글 상세 조회(`GET /api/v1/posts/{postId}`) 응답에 댓글/대댓글 포함

---

## 구현 순서

```
1. 예외 클래스 확장
   └── NotFoundException에 comment(), reply() 추가
   └── ForbiddenException에 commentUpdate(), commentDelete() 추가

2. DTO 폴더 구조 정비 및 DTO 생성
   └── dto/comments/ 폴더 생성
   └── 10개 DTO 생성 (아래 목록 참조)

3. CommentRepository 메서드 추가
   └── findAllByPostWithChildren 구현

4. CommentService 구현
   └── 댓글 CRUD
   └── 답글 CRUD (/replies 엔드포인트 사용)

5. CommentController 구현
   └── @RequestMapping("/api/v1/posts/{postId}/comments")
   └── 댓글 엔드포인트 (POST, PUT, DELETE)
   └── 답글 엔드포인트 (POST, PUT, DELETE) - /{commentId}/replies

6. 게시글 상세 조회에 댓글 포함
   └── PostResponse에 comments 필드 추가 (List<CommentWithRepliesResponse>)
   └── PostService.getPost() 수정

7. 테스트 코드 작성
   └── CommentServiceTest
   └── CommentControllerTest
```

---

## 생성할 파일

### DTO (`dto/comments/`)

```
dto/comments/
├── CommentCreatePostRequest.java     # POST .../comments
│   └── content
├── CommentUpdatePutRequest.java      # PUT .../comments/{id}
│   └── content
├── CommentPostResponse.java          # 댓글 작성 응답
│   └── commentId, content, author, createdAt
├── CommentPutResponse.java           # 댓글 수정 응답
│   └── commentId, content, author, updatedAt
├── CommentWithRepliesResponse.java   # 게시글 상세 조회용
│   └── commentId, content, author, createdAt, updatedAt, replies[]
├── ReplyCreatePostRequest.java       # POST .../replies
│   └── content
├── ReplyUpdatePutRequest.java        # PUT .../replies/{id}
│   └── content
├── ReplyPostResponse.java            # 답글 작성 응답
│   └── replyId, content, author, parentCommentId, createdAt
├── ReplyPutResponse.java             # 답글 수정 응답
│   └── replyId, content, author, parentCommentId, updatedAt
└── ReplyResponse.java                # 게시글 상세 조회 내 replies용
    └── replyId, content, author, createdAt, updatedAt
```

### DTO 주석 규칙 (v-log-dto-convention.md 준수)

```java
/**
 * POST /api/v1/posts/{postId}/comments 요청 객체
 */
public class CommentCreatePostRequest {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
```

### Service/Controller
- `service/CommentService.java`
- `controller/CommentController.java`

### 테스트
- `CommentServiceTest.java`
- `CommentControllerTest.java`

---

## 수정할 파일

- `exception/NotFoundException.java` - comment(), reply() 메서드 추가
- `exception/ForbiddenException.java` - commentUpdate(), commentDelete() 메서드 추가
- `repository/CommentRepository.java` - findAllByPostWithChildren 메서드 추가
- `dto/response/PostResponse.java` - comments 필드 추가
- `service/PostService.java` - getPost()에 댓글 조회 로직 추가

---

## 핵심 검증 로직

1. **depth 제한**: 답글은 1-depth만 허용 (답글의 답글 불가)
2. **소속 검증**: commentId가 postId의 댓글인지, replyId가 commentId의 답글인지
3. **권한 검증**: 수정/삭제는 작성자만 가능

---

## 응답 형식 (v-log-dto-convention.md 준수)

모든 응답은 `ApiResponse<T>`로 래핑:

```java
// 댓글 작성 (201 Created)
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponse.success("댓글 작성 성공", response));

// 댓글 수정 (200 OK)
return ResponseEntity.ok(ApiResponse.success("댓글 수정 성공", response));

// 댓글 삭제 (204 No Content)
return ResponseEntity.noContent().build();
```
