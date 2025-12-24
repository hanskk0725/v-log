package com.likelion.vlog.entity;

import com.likelion.vlog.dto.auth.SignupRequestDto;
import com.likelion.vlog.dto.user.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CurrentTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Blog blog;

//    @OneToMany(mappedBy = "user")
//    private List<Comment> comments =  new ArrayList<>();

    private String email;
    private String password;
    private String nickname;

    @CurrentTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        Blog blog = new  Blog();
        blog.setUser(this);
        blog.setTitle(this.nickname + "의 블로그");
        this.blog = blog;
    }


    public void upDateInfo(UserUpdateRequestDto requestDto, PasswordEncoder passwordEncoder){

        if (requestDto.getNickname() != null) {
            this.nickname = requestDto.getNickname();
        }

        if (requestDto.getPassword() != null) {
            this.password = passwordEncoder.encode(requestDto.getPassword());
        }
    }


    public static User of(SignupRequestDto signupRequestDto, PasswordEncoder passwordEncoder){
        User user = new User();
        user.setEmail(signupRequestDto.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        user.setNickname(signupRequestDto.getNickname());
        return user;
    }

}
