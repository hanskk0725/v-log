package com.likelion.vlog.dto.posts.response;

import com.likelion.vlog.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회 응답 DTO
 * - 게시글 전체 내용 포함
 * - 좋아요/댓글은 Sprint 2에서 구현 예정
 */
@Getter
@Builder
public class PostResponse {
    private Long postId;
    private String title;
    private String content;
    private AuthorResponse author;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 정적 팩토리 메서드
     */
    public static PostResponse of(Post post, List<String> tags) {
        return PostResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorResponse.from(post.getBlog().getUser()))
                .tags(tags)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}