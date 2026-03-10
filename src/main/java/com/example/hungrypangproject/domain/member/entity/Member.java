package com.example.hungrypangproject.domain.member.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.member.dto.request.SaveMemberRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String phoneNo;

    @Column(nullable = false, length = 225)
    private String address;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false)
    private Long point;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRoleEnum role;

    @Column(nullable = false)
    private Long totalPriceAmount;

    private String refreshToken;

    private LocalDateTime deletedAt;

    public static Member register(SaveMemberRequest request, String encodedPassword) {
        Member member = new Member();
        member.nickname = request.getNickname();
        member.email = request.getEmail();
        member.address = request.getAddress();
        member.phoneNo = request.getPhoneNo();
        member.password = encodedPassword;
        member.point = 0L;
        member.role = MemberRoleEnum.ROLE_USER;
        member.totalPriceAmount = 0L;
        return member;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateInfo(String nickname, String address, String phoneNo){
        if(nickname != null && !nickname.isBlank()){
            this.nickname = nickname;
            this.address = address;
            this.phoneNo = phoneNo;
        }
    }

    public void updateRole(MemberRoleEnum role) {
        this.role = role;
    }

}
