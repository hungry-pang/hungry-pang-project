package com.example.hungrypangproject.domain.point.entity;

import com.example.hungrypangproject.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long currentlyPoint;

    @Column(nullable = false)
    private Long usedPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PointEnum status;

    @Column(nullable = false)
    private LocalDateTime saveAt;

    @Column(nullable = false)
    private LocalDateTime expireAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    private LocalDateTime modifiedAt;

    public static Point register(
            Long orderId,
            Long currentlyPoint,
            Long usedPoint,
            PointEnum status,
            Member member,
            LocalDateTime saveAt,
            LocalDateTime expiredAt
    ) {
        Point point = new Point();

        point.orderId = orderId;
        point.currentlyPoint = currentlyPoint;
        point.usedPoint = usedPoint;
        point.status = status;
        point.member = member;
        point.saveAt = LocalDateTime.now();
        point.expireAt = LocalDateTime.now().plusYears(1);

        return point;
    }

}
