package com.likelion.vlog.dto.posts.response;

import com.likelion.vlog.dto.comments.CommentWithRepliesResponse;
import com.likelion.vlog.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회 응답 DTO
 * - 게시글 전체 내용 포함
 */
@Getter
@Builder
public class PostResponse {
    private Long postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private List<String> tags;
    private List<CommentWithRepliesResponse> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 정적 팩토리 메서드 (댓글 포함)
     */
    public static PostResponse of(Post post, List<String> tags, List<CommentWithRepliesResponse> comments) {
        return PostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .tags(tags)
                .comments(comments)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    /**
     * 정적 팩토리 메서드 (댓글 미포함 - 작성/수정 응답용)
     */
    public static PostResponse of(Post post, List<String> tags) {
        return of(post, tags, List.of());
    }
}