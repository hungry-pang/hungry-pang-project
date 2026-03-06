package com.example.hungrypangproject.domain.membership.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "userMembers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMembership {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userMembershipId;

    @Column(nullable = false)
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberships")
    private Membership membership;

    @Column(nullable = false)
    private Double totalAmount;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static UserMembership userMembership (
            Long memberId,
            Membership membership,
            Double totalAmount
    ) {
        UserMembership userMembership = new UserMembership();

        userMembership.memberId = memberId;
        userMembership.membership = membership;
        userMembership.totalAmount =totalAmount;

        return userMembership;
    }



}
