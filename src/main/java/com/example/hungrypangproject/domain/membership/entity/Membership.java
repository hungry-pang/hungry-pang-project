package com.example.hungrypangproject.domain.membership.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "memberships")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private BigDecimal earnRate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GradeEnum grade;

    @Column(nullable = false)
    private BigDecimal maxTotalPaidAmount;

    @Column(nullable = false)
    private BigDecimal minTotalPaidAmount;

    @Column(nullable = false, length = 50)
    private String description;

    public static Membership register(
            BigDecimal earnRate,
            GradeEnum grade,
            BigDecimal maxTotalPaidAmount,
            BigDecimal minTotalPaidAmount,
            String description
    ) {
        Membership membership = new Membership();

        membership.earnRate = earnRate;
        membership.grade = grade;
        membership.maxTotalPaidAmount = maxTotalPaidAmount;
        membership.minTotalPaidAmount = minTotalPaidAmount;
        membership.description = description;

        return membership;
    }
}
