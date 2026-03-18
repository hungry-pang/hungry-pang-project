package com.example.hungrypangproject.domain.point.service;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("포인트 사용 시 동시에 10개의 요청이 와도 1번만 차감되어야 한다")
    void concurrencyTest() throws InterruptedException {
        Member member = memberRepository.save(Member.builder()
                .email("test@test.com")
                .nickname("test12")
                .address("강남구 123번지")
                .password("12345678")
                .phoneNo("010-1111-1111")
                .role(MemberRoleEnum.ROLE_USER)
                .totalPriceAmount(new BigDecimal("100000"))
                .totalPoint(new BigDecimal("10000"))
                .build());
        Order order = orderRepository.save(Order.builder()
                .totalPrice(new BigDecimal("50000"))
                .id(member.getMemberId())
                .build());
        BigDecimal useAmount = new BigDecimal("1000");

        // 동시 요청 수
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 실행 (10개의 스레드가 동시에 pointService.usedPoint 호출)
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Order currentOrder = orderRepository.findById(order.getId()).orElseThrow();
                    Member currentMember = memberRepository.findById(member.getMemberId()).orElseThrow();

                    pointService.usedPoint(currentMember, currentOrder, useAmount);

                } catch (Exception e) {
                    System.out.println("동시성 제어에 의해 차단됨: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });

            // 모든 스레드가 종료될 때까지 대기
            latch.await();

            // 결과 검증
            Member updatedMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();

            Assertions.assertThat(updatedMember.getTotalPoint())
                    .isEqualByComparingTo(new BigDecimal("9000"));
        }
    }
}
