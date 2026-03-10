package com.example.hungrypangproject.domain.member.dto.request;

import lombok.Getter;

@Getter
public class LoginMemberRequest {
    private String email;
    private String password;
}
