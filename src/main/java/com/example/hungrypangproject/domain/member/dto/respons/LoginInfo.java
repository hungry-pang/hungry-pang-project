package com.example.hungrypangproject.domain.member.dto.respons;

import com.example.hungrypangproject.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginInfo {

    private final boolean success;
    private final Long id;
    private final String email;
    private final String accessToken;
    private final String refreshToken;

    public static LoginInfo register(Member member, String accessToken, String refreshToken) {
        return new LoginInfo(true, member.getMemberId(), member.getEmail(), accessToken, refreshToken);
    }
}
