package com.likelion.vlog.dto.user;

import com.likelion.vlog.entity.User;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDto { //유저 상세정보

    private Long id;
    private String email;
    private String nickname;
    private Long blogId;
    private String blogTitle;

    public static UserDto of(User user){
        Long id = user.getId();
        String email = user.getEmail();
        String nickname = user.getNickname();
        Long blogId = user.getBlog().getId();
        String blogTitle = user.getBlog().getTitle();
        return new UserDto(id, email, nickname, blogId, blogTitle);
    }
}
