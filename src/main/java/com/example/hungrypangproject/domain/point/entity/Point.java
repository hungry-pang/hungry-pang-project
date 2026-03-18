package com.example.hungrypangproject.domain.point.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import org.aspectj.weaver.ast.Or;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "points")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Point extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    // 현재 총 포인트
    @Column(nullable = false)
    private BigDecimal currentlyPoint;

    // 적립 포인트
    @Column(nullable = false)
    private BigDecimal earnPoint;

    // 사용 포인트
    @Column(nullable = false)
    private BigDecimal usedPoint;

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
            BigDecimal currentlyPoint,
            BigDecimal earnPoint,
            BigDecimal usedPoint,
            PointEnum status,
            Member member,
            Order order
    ) {
        Point point = new Point();

        point.currentlyPoint = (currentlyPoint != null) ? currentlyPoint:BigDecimal.ZERO;
        point.earnPoint = (earnPoint != null) ? earnPoint:BigDecimal.ZERO;
        point.usedPoint = (usedPoint != null) ? usedPoint:BigDecimal.ZERO;
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
