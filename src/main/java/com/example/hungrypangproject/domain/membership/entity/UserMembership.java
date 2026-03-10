package com.example.hungrypangproject.domain.membership.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "userMembers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMembership extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userMembershipId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "memberships")
    private Membership membership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "members")
    private Member member;

    public static UserMembership register (
            Long memberId,
            Membership membership,
            Long totalPrice
    ) {
        UserMembership userMembership = new UserMembership();

        userMembership.memberId = memberId;
        userMembership.membership = membership;
        userMembership.totalPrice =totalPrice;

        return userMembership;
    }



}
