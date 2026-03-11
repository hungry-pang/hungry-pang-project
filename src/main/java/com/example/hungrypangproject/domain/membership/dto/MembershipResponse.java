package com.example.hungrypangproject.domain.membership.dto;

import com.example.hungrypangproject.domain.membership.entity.GradeEnum;
import com.example.hungrypangproject.domain.membership.entity.UserMembership;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MembershipResponse {

    private final Long memberId;
    private final BigDecimal earnRate;
    private final GradeEnum grade;
    private final BigDecimal remainAmount;

    public static MembershipResponse register(UserMembership userStatus, BigDecimal remainAmount) {
        return new MembershipResponse(
                userStatus.getMember().getMemberId(),
                userStatus.getMembership().getEarnRate(),
                userStatus.getMembership().getGrade(),
                remainAmount
        );
    }
}