package com.likelion.vlog.dto.users;

import com.likelion.vlog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateResponse {
    private Long id;
    private String nickname;
    private String email;

    public UserUpdateResponse(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
    }

    public static UserUpdateResponse of(User user){
        return  new UserUpdateResponse(user.getId(), user.getNickname(), user.getEmail());
    }
}
