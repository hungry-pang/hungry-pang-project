package com.example.hungrypangproject.domain.point.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Or;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 현재 총 포인트
    @Column(nullable = false)
    private Long currentlyPoint;

    // 적립 포인트
    @Column(nullable = false)
    private Long earnPoint;

    // 사용 포인트
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
    @JoinColumn(nullable = false, name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "order_id")
    private Order order;

    public static Point register(
            Long currentlyPoint,
            Long earnPoint,
            Long usedPoint,
            PointEnum status,
            Member member,
            Order order
    ) {
        Point point = new Point();

        point.currentlyPoint = (currentlyPoint != null) ? currentlyPoint:0L;
        point.earnPoint = (earnPoint != null) ? earnPoint:0L;
        point.usedPoint = (usedPoint != null) ? usedPoint:0L;
        point.status = status;
        point.member = member;
        point.order = order;
        point.saveAt = LocalDateTime.now();
        point.expireAt = LocalDateTime.now().plusYears(1);

        return point;
    }

    // 배달 완료 시 상태 변경
    public void activate() {
        if (this.status == PointEnum.HOLDING) {
            this.status = PointEnum.SAVE;
        }
    }
}
