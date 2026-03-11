package com.example.hungrypangproject.domain.member.dto.request;

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
    private BigDecimal totalPoint;
}
