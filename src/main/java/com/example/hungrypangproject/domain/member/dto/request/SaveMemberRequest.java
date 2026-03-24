package com.example.hungrypangproject.domain.member.dto.request;

import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveMemberRequest {
    private String nickname;
    private String email;
    private String address;
    private String phoneNo;
    private String password;
    private MemberRoleEnum role;
    private BigDecimal totalPoint;
}
