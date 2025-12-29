package com.likelion.vlog.dto.users;

import com.likelion.vlog.entity.User;
import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGetResponse { //유저 상세정보

    private Long id;
    private String email;
    private String nickname;
    private Long blogId;
    private String blogTitle;

    public static UserGetResponse of(User user){
        Long id = user.getId();
        String email = user.getEmail();
        String nickname = user.getNickname();
        Long blogId = user.getBlog().getId();
        String blogTitle = user.getBlog().getTitle();
        return new UserGetResponse(id, email, nickname, blogId, blogTitle);
    }
}
