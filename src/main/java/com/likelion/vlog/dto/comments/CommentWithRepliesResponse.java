package com.likelion.vlog.dto.comments;

import com.likelion.vlog.dto.posts.response.AuthorResponse;
import com.likelion.vlog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글 상세 조회용 댓글 응답 객체 (대댓글 포함)
 */
@Getter
@Builder
public class CommentWithRepliesResponse {

    private Long commentId;
    private String content;
    private AuthorResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReplyResponse> replies;

    public static CommentWithRepliesResponse from(Comment comment) {
        return CommentWithRepliesResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .author(AuthorResponse.from(comment.getUser()))
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(comment.getChildren().stream()
                        .map(ReplyResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
