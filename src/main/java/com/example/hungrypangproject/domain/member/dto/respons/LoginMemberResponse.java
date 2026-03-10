package com.example.hungrypangproject.domain.member.dto.respons;

import com.example.hungrypangproject.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginMemberResponse {

    private final Boolean success;
    private final Long id;
    private final String email;

    public static LoginMemberResponse register (LoginInfo info){
        return new LoginMemberResponse(
                true,
                info.getId(),
                info.getEmail()
        );
    }
}
