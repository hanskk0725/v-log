package com.likelion.vlog.service;

import com.likelion.vlog.dto.request.PostCreateRequest;
import com.likelion.vlog.dto.request.PostUpdateRequest;
import com.likelion.vlog.dto.response.PostResponse;
import com.likelion.vlog.entity.Blog;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private TagMapRepository tagMapRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BlogRepository blogRepository;

    private User user;
    private Blog blog;
    private Post post;

    @BeforeEach
    void setUp() {
        user = createTestUser(1L, "test@test.com", "테스터");
        blog = createTestBlog(1L, user);
        post = createTestPost(1L, "테스트 제목", "테스트 내용", blog);
    }

    @Nested
    @DisplayName("게시글 상세 조회")
    class GetPost {

        @Test
        @DisplayName("존재하는 게시글 조회 성공")
        void getPost_Success() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            PostResponse response = postService.getPost(1L);

            // then
            assertThat(response.getPostId()).isEqualTo(1L);
            assertThat(response.getTitle()).isEqualTo("테스트 제목");
            assertThat(response.getContent()).isEqualTo("테스트 내용");
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
        void getPost_NotFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPost(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("게시글 작성")
    class CreatePost {

        @Test
        @DisplayName("게시글 작성 성공")
        void createPost_Success() {
            // given
            PostCreateRequest request = new PostCreateRequest();
            ReflectionTestUtils.setField(request, "title", "새 게시글");
            ReflectionTestUtils.setField(request, "content", "새 내용");
            ReflectionTestUtils.setField(request, "tags", List.of("Spring", "JPA"));

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(blogRepository.findByUser(user)).willReturn(Optional.of(blog));
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
                Post savedPost = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedPost, "id", 1L);
                ReflectionTestUtils.setField(savedPost, "tagMapList", new ArrayList<>());
                return savedPost;
            });

            // when
            PostResponse response = postService.createPost(request, "test@test.com");

            // then
            assertThat(response.getTitle()).isEqualTo("새 게시글");
            assertThat(response.getContent()).isEqualTo("새 내용");
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 게시글 작성 시 예외 발생")
        void createPost_UserNotFound() {
            // given
            PostCreateRequest request = new PostCreateRequest();
            given(userRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.createPost(request, "unknown@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("게시글 수정 성공")
        void updatePost_Success() {
            // given
            PostUpdateRequest request = new PostUpdateRequest();
            ReflectionTestUtils.setField(request, "title", "수정된 제목");
            ReflectionTestUtils.setField(request, "content", "수정된 내용");
            ReflectionTestUtils.setField(request, "tags", List.of());

            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            PostResponse response = postService.updatePost(1L, request, "test@test.com");

            // then
            assertThat(response.getTitle()).isEqualTo("수정된 제목");
            assertThat(response.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 수정 시 예외 발생")
        void updatePost_Forbidden() {
            // given
            PostUpdateRequest request = new PostUpdateRequest();
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.updatePost(1L, request, "other@test.com"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("수정 권한이 없습니다");
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePost {

        @Test
        @DisplayName("게시글 삭제 성공")
        void deletePost_Success() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            postService.deletePost(1L, "test@test.com");

            // then
            verify(tagMapRepository).deleteAllByPost(post);
            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 삭제 시 예외 발생")
        void deletePost_Forbidden() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.deletePost(1L, "other@test.com"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("삭제 권한이 없습니다");
        }
    }

    // 테스트 헬퍼 메서드
    private User createTestUser(Long id, String email, String nickname) {
        try {
            java.lang.reflect.Constructor<User> constructor = User.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            User user = constructor.newInstance();
            ReflectionTestUtils.setField(user, "id", id);
            ReflectionTestUtils.setField(user, "email", email);
            ReflectionTestUtils.setField(user, "nickname", nickname);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Blog createTestBlog(Long id, User user) {
        try {
            java.lang.reflect.Constructor<Blog> constructor = Blog.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Blog blog = constructor.newInstance();
            ReflectionTestUtils.setField(blog, "id", id);
            ReflectionTestUtils.setField(blog, "user", user);
            ReflectionTestUtils.setField(blog, "title", user.getNickname() + "의 블로그");
            return blog;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Post createTestPost(Long id, String title, String content, Blog blog) {
        Post post = Post.create(title, content, blog);
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "tagMapList", new ArrayList<>());
        return post;
    }
}
