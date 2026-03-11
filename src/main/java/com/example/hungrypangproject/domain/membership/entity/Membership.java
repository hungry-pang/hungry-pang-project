package com.example.hungrypangproject.domain.membership.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double earnRate;

    @Enumerated(EnumType.STRING)
    private GradeEnum grade;

    @Column(nullable = false)
    private Double minTotalPaidAmount;


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
