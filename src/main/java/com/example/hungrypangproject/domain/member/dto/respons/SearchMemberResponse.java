package com.example.hungrypangproject.domain.member.dto.respons;

import com.example.hungrypangproject.domain.member.entity.Member;
import lombok.*;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SearchMemberResponse {

    private Long memberId;
    private String email;
    private String nickName;
    private String address;
    private String phoneNo;
    private BigDecimal pointBalance;

    public static SearchMemberResponse register(Member member) {
        return new SearchMemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getAddress(),
                member.getPhoneNo(),
                member.getTotalPriceAmount()
        );
    }
}
