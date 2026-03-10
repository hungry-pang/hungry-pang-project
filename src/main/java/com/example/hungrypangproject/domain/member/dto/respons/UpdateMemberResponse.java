package com.example.hungrypangproject.domain.member.dto.respons;

import com.example.hungrypangproject.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateMemberResponse {

    private final Long memberId;
    private final String nickname;
    private final String address;
    private final String phoneNo;
    private final Long point;

    public static UpdateMemberResponse register(Member member) {
        return new UpdateMemberResponse(
                member.getMemberId(),
                member.getNickname(),
                member.getAddress(),
                member.getPhoneNo(),
                member.getTotalPoint()
        );
    }
}
