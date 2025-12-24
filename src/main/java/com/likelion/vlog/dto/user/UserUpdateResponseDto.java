package com.likelion.vlog.dto.user;

import com.likelion.vlog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateResponseDto {
    private Long id;
    private String nickname;
    private String email;

    public UserUpdateResponseDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
    }

    public static UserUpdateResponseDto of(User user){
        return  new UserUpdateResponseDto(user.getId(), user.getNickname(), user.getEmail());
    }
}
