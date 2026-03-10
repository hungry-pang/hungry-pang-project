package com.example.hungrypangproject.domain.point.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.member.entity.Member;
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
import org.springframework.http.HttpStatus;

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

    @InjectMocks
    private PointService pointService;

    private Member member;
    private Order order;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .totalPoint(1000L)
                .build();
        order = Order.builder()
                .id(1L)
                .orderNum(UUID.fromString(UUID.randomUUID().toString()))
                .totalPrice(BigDecimal.valueOf(10000))
                .build();
    }

    @Test
    @DisplayName("포인트 적립 : 정확히 5% 계산이 되는지 확인")
    void calculateEarnedPointsTest() {

        // when
        Long earnedPoints = pointService.calculateEarnedPoints(10000L,0L);

        // then
        assertEquals(500L, earnedPoints);
    }

    @Test
    @DisplayName("포인트 사용 실페 : 결제 금액의 10% 초과 시 에러")
    void usePointTest() {
        // given
        Long useAmount = 1500L;

        // when & then
        ServiceException exception = assertThrows(ServiceException.class,
                () -> {
            pointService.usedPoint(member, order, useAmount);
                });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(ErrorCode.POINT_EXCEED_LIMIT.getMessage(), exception.getMessage());}

    @Test
    @DisplayName("배달 완료 시 포인트 적립 확정")
    void completePoint_Success() {
        // given
        Point holdingPoint = Point.register(1000L,500L,10L, PointEnum.HOLDING,member,order);
        when(pointRepository.findFirstByOrderAndStatusOrderByCreatedAtDesc(order,PointEnum.HOLDING))
                .thenReturn(Optional.of(holdingPoint));

        // when
        pointService.completePoint(order);

        // then
        assertEquals(PointEnum.SAVE, holdingPoint.getStatus()); // 상태가 save로 변경
        assertEquals(1500L, member.getTotalPoint());
    }
}
