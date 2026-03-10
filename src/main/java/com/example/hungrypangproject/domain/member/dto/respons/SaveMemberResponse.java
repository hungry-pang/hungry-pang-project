package com.example.hungrypangproject.domain.member.dto.respons;

import com.example.hungrypangproject.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SaveMemberResponse {
    private final Long memberId;
    private final String nickname;
    private final String email;
    private final String address;
    private final String phoneNo;

    public static SaveMemberResponse register(Member member){
        return new SaveMemberResponse(
                member.getMemberId(),
                member.getNickname(),
                member.getEmail(),
                member.getAddress(),
                member.getPhoneNo()
        );
    }
}
