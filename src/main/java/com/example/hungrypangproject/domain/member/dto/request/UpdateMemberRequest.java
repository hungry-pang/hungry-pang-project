package com.example.hungrypangproject.domain.member.dto.request;

import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRequest {
    private String nickname;
    private String phoneNo;
    private String address;
    private MemberRoleEnum role;
}
