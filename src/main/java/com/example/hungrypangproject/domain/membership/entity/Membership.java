package com.example.hungrypangproject.domain.membership.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long MembershipId;

    @Column(nullable = false)
    private Double earnRate;

    @Enumerated(EnumType.STRING)
    private GradeEnum grade;

    @Column(nullable = false)
    private Double minTotalPaidAmount;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static Membership register(
            Double earnRate,
            GradeEnum grade,
            Double minTotalPaidAmount
    ) {
        Membership membership = new Membership();

        membership.earnRate = 0.5;
        membership.grade = grade;
        membership.minTotalPaidAmount = minTotalPaidAmount;

        return membership;
    }
}
