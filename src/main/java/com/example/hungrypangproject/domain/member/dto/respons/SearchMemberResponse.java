package com.example.hungrypangproject.domain.member.dto.respons;

import com.example.hungrypangproject.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchMemberResponse {

    private final String email;
    private final String nickName;
    private final String address;
    private final String phoneNo;
    private final BigDecimal pointBalance;

    public static SearchMemberResponse register(Member member) {
        return new SearchMemberResponse(
                member.getEmail(),
                member.getNickname(),
                member.getAddress(),
                member.getPhoneNo(),
                member.getTotalPriceAmount()
        );
    }
}
