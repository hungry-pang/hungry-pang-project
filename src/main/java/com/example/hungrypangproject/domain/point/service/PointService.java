package com.example.hungrypangproject.domain.point.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.point.entity.Point;
import com.example.hungrypangproject.domain.point.entity.PointEnum;
import com.example.hungrypangproject.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j(topic = "PointService")
@Service
@RequiredArgsConstructor
public class PointService {

    /*
     * 1. 적립 포인트 계산 : 실 결제 금액의 5% 적립
     * 2. 포인트 사용 및 로그 생성
     * 3. 포인트 적립 및 로그 생성
     * 4. 배달 완료 : 포인트 확정 업데이트
     * 주문 서비스와 동시에 롤백이 되지 않게 Transactional 삭제
     */

      private final PointRepository pointRepository;
    private final MemberRepository memberRepository;
//
//    @Transactional
//    public void withdraw(Long memberId, int amount) {
//        Point point = pointRepository.findById(memberId).orElseThrow();
//
//        point.decrease(amount);
//        log.info("[사용 완료] {} 에서 실행 되었습니다. 잔여 포인트 : " + Thread.currentThread().getName(), point.getCurrentlyPoint());
//    }

    public BigDecimal calculateEarnedPoints (BigDecimal totalPrice, BigDecimal usedPoints) {
        BigDecimal payAmount = totalPrice.subtract(usedPoints);
        return payAmount.multiply(new BigDecimal("0.05"))
                .setScale(0,RoundingMode.FLOOR);
    }

    @Transactional
    public void usedPoint(Member member, Order order, BigDecimal useAmount) {
        // 락 걸고 회원 정보 다시 가져오기
        Member lockdMember = memberRepository.findByaTotalPointForLock(member.getTotalPoint())
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        if(useAmount.compareTo(new BigDecimal("100")) < 0) {
            throw new ServiceException(ErrorCode.POINT_NOT_ENOUGH);
        }

        // 결제 금액의 10% 계산
       BigDecimal totalPrice = order.getTotalPrice();
        BigDecimal maxLimit = totalPrice.multiply(new BigDecimal("0.1"))
                .setScale(0, RoundingMode.FLOOR);

        // 사용하려는 포인트가 10% 초과할 때
        if(useAmount.compareTo(maxLimit) > 0) {
            throw new ServiceException(ErrorCode.POINT_EXCEED_LIMIT);
        }

        // 사용가능한 포인트에서 즉시 차감
        lockdMember.minusPoint(useAmount);

        Point useLog = Point.register(
                lockdMember.getTotalPoint(),
                BigDecimal.ZERO,
                useAmount,
                PointEnum.HOLDING,
                member,
                order
        );
        pointRepository.save(useLog);
   }

    public void reserveEarnPoint(Member member, Order order, BigDecimal earnAmount) {
        // 배달 완료 전 홀딩 상태 포인트 확인
        Point earnLog = Point.register(
                member.getTotalPoint(),
                earnAmount,
                BigDecimal.ZERO,
                PointEnum.HOLDING,
                member,
                order
        );
        pointRepository.save(earnLog);
   }

    public void completePoint(Order order) {
       // 적립 홀딩 상태 확인
       Point point = pointRepository.findFirstByOrderAndStatusOrderByCreatedAtDesc(order, PointEnum.HOLDING)
               .orElseThrow(() -> new ServiceException(ErrorCode.POINT_NOT_HOLDING));

       // 상태 변경 및
       point.activate();
       point.getMember().addPoint(point.getEarnPoint());

       log.info("포인트 적립 완료: Member={}, Amount={}",point.getMember(), point.getId(), point.getEarnPoint());
   }
}
