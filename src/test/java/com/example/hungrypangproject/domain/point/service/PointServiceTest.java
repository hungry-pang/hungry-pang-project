package com.example.hungrypangproject.domain.point.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.point.entity.Point;
import com.example.hungrypangproject.domain.point.entity.PointEnum;
import com.example.hungrypangproject.domain.point.repository.PointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private PointService pointService;

    private Member member;
    private Order order;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .totalPoint(BigDecimal.valueOf(1000))
                .build();
        order = Order.builder()
                .id(1L)
                .orderNum(UUID.fromString(UUID.randomUUID().toString()))
                .totalPrice(BigDecimal.valueOf(10000))
                .build();
    }

    @Test
    @DisplayName("포인트 적립 : 5% 계산 확인")
    void calculateEarnedPointsTest() {

        // when
        BigDecimal earnedPoints = pointService.calculateEarnedPoints(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(0));

        // then
        assertEquals(BigDecimal.valueOf(50), earnedPoints);
    }

    @Test
    @DisplayName("포인트 사용 실페 : 결제 금액의 10% 초과 시 에러")
    void usePointTest() {
        // given
        BigDecimal useAmount = BigDecimal.valueOf(1500);

        when(memberRepository.findByMemberIdForLock(member.getMemberId()))
                .thenReturn(Optional.of(member));

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> {
            pointService.usedPoint(member, order, useAmount);
                });
        assertEquals(400,exception.getStatus().value());
        assertEquals(ErrorCode.POINT_EXCEED_LIMIT.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("배달 완료 시 포인트 적립 확정")
    void completePoint_Success() {
        // given
        Point holdingPoint = Point.register(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(10),
                PointEnum.HOLDING,member,order);
        when(pointRepository.findFirstByOrderAndStatusOrderByCreatedAtDesc(order,PointEnum.HOLDING))
                .thenReturn(Optional.of(holdingPoint));

        // when
        pointService.completePoint(order);

        // then
        assertEquals(PointEnum.SAVE, holdingPoint.getStatus()); // 상태가 save로 변경
        assertEquals(BigDecimal.valueOf(1500), member.getTotalPoint());
    }
}
