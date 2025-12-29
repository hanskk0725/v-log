package com.likelion.vlog.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * POST /auth/signin 요청 객체
 */
@Getter
public class SignupRequest {
    @NotBlank
    @Email
    private  String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nickname;
}
