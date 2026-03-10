package com.example.hungrypangproject.domain.member.dto.request;

import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import lombok.Getter;

@Getter
public class SaveMemberRequest {
    private String nickname;
    private String email;
    private String address;
    private String phoneNo;
    private String password;
    private Long point;
    private MemberRoleEnum role;
}
