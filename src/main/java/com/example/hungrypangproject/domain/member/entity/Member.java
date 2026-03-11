package com.example.hungrypangproject.domain.member.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.member.dto.request.SaveMemberRequest;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
    private BigDecimal totalPoint;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRoleEnum role;

    @Column(nullable = false)
    private BigDecimal totalPriceAmount;

    private String refreshToken;

    private LocalDateTime deletedAt;

    public static Member register(SaveMemberRequest request, String encodedPassword) {
        Member member = new Member();
        member.nickname = request.getNickname();
        member.email = request.getEmail();
        member.address = request.getAddress();
        member.phoneNo = request.getPhoneNo();
        member.password = encodedPassword;
        member.totalPoint = BigDecimal.ZERO;
        member.role = MemberRoleEnum.ROLE_USER;
        member.totalPriceAmount = BigDecimal.ZERO;
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

    // 포인트 적립 (배달 완료 후 적용)
    public void addPoint(BigDecimal amount) {
        if(amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(ErrorCode.POINT_NOT_ENOUGH);
        }
        this.totalPoint = this.totalPoint.add(amount);
    }

    // 포인트 사용 (차감)
    public void minusPoint(BigDecimal amount) {
        if(amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(ErrorCode.POINT_NOT_ENOUGH);
        }
        if(this.totalPoint.compareTo(amount) < 0) {
            throw new ServiceException(ErrorCode.POINT_EXCEED_LIMIT);
        }
        this.totalPoint = this.totalPoint.subtract(amount);
    }
}
