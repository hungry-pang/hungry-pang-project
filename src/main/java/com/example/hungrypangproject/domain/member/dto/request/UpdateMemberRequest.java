package com.example.hungrypangproject.domain.member.dto.request;

import lombok.Getter;

@Getter
public class UpdateMemberRequest {
    private String nickname;
    private String phoneNo;
    private String address;
}
