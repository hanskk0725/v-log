package com.likelion.vlog.service;

import com.likelion.vlog.dto.auth.SignupRequestDto;
import com.likelion.vlog.dto.user.UserDto;
import com.likelion.vlog.entity.Blog;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("회원가입 성공")
        void signup_Success() {
            // given
            SignupRequestDto dto = new SignupRequestDto();
            ReflectionTestUtils.setField(dto, "email", "test@test.com");
            ReflectionTestUtils.setField(dto, "password", "password123");
            ReflectionTestUtils.setField(dto, "nickname", "테스터");

            given(userRepository.existsByEmail("test@test.com")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "id", 1L);
                // Blog 설정 (UserDto.of()에서 필요 - @PrePersist 대신)
                try {
                    java.lang.reflect.Constructor<Blog> blogConstructor = Blog.class.getDeclaredConstructor();
                    blogConstructor.setAccessible(true);
                    Blog blog = blogConstructor.newInstance();
                    ReflectionTestUtils.setField(blog, "id", 1L);
                    ReflectionTestUtils.setField(blog, "title", user.getNickname() + "의 블로그");
                    ReflectionTestUtils.setField(user, "blog", blog);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return user;
            });

            // when
            UserDto result = authService.signup(dto);

            // then
            assertThat(result.getEmail()).isEqualTo("test@test.com");
            assertThat(result.getNickname()).isEqualTo("테스터");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 시 예외 발생")
        void signup_DuplicateEmail() {
            // given
            SignupRequestDto dto = new SignupRequestDto();
            ReflectionTestUtils.setField(dto, "email", "existing@test.com");

            given(userRepository.existsByEmail("existing@test.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 존재하는 이메일");
        }
    }

    @Nested
    @DisplayName("사용자 조회 (UserDetailsService)")
    class LoadUserByUsername {

        @Test
        @DisplayName("존재하는 사용자 조회 성공")
        void loadUserByUsername_Success() {
            // given
            User user = createTestUser(1L, "test@test.com", "encodedPassword", "테스터");
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));

            // when
            UserDetails userDetails = authService.loadUserByUsername("test@test.com");

            // then
            assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
            assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void loadUserByUsername_NotFound() {
            // given
            given(userRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.loadUserByUsername("unknown@test.com"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("사용자 정보 조회")
    class GetUserInfo {

        @Test
        @DisplayName("사용자 정보 조회 성공")
        void getUserInfo_Success() {
            // given
            User user = createTestUser(1L, "test@test.com", "encodedPassword", "테스터");
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));

            // when
            UserDto result = authService.getUserInfo("test@test.com");

            // then
            assertThat(result.getEmail()).isEqualTo("test@test.com");
            assertThat(result.getNickname()).isEqualTo("테스터");
        }

        @Test
        @DisplayName("존재하지 않는 사용자 정보 조회 시 예외 발생")
        void getUserInfo_NotFound() {
            // given
            given(userRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.getUserInfo("unknown@test.com"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }

    // 헬퍼 메서드
    private User createTestUser(Long id, String email, String password, String nickname) {
        try {
            java.lang.reflect.Constructor<User> constructor = User.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            User user = constructor.newInstance();
            ReflectionTestUtils.setField(user, "id", id);
            ReflectionTestUtils.setField(user, "email", email);
            ReflectionTestUtils.setField(user, "password", password);
            ReflectionTestUtils.setField(user, "nickname", nickname);

            // Blog 설정 (UserDto.of()에서 필요)
            java.lang.reflect.Constructor<Blog> blogConstructor = Blog.class.getDeclaredConstructor();
            blogConstructor.setAccessible(true);
            Blog blog = blogConstructor.newInstance();
            ReflectionTestUtils.setField(blog, "id", 1L);
            ReflectionTestUtils.setField(blog, "title", nickname + "의 블로그");
            ReflectionTestUtils.setField(user, "blog", blog);

            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
