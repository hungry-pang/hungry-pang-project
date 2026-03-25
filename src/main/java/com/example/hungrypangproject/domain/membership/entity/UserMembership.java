package com.example.hungrypangproject.domain.membership.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "user_members")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMembership extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_membership_id")
    private Long id;

    @Version
    private Long version;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "membership_id")
    private Membership membership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "member_id")
    private Member member;

    public static UserMembership register (
            Member member,
            Membership defaultGrade
    ) {
        UserMembership userMembership = new UserMembership();

        userMembership.member = member;
        userMembership.membership = defaultGrade;
        userMembership.totalPrice = BigDecimal.ZERO;

        return userMembership;
    }

    // 금액 누적 및 등급 변경
    public void updateStatus(BigDecimal addAmount,Membership membership) {
        this.totalPrice = this.totalPrice.add(addAmount);
        this.membership = membership;
    }


}
