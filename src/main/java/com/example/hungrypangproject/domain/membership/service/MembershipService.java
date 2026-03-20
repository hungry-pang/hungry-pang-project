package com.example.hungrypangproject.domain.membership.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.membership.dto.MembershipResponse;
import com.example.hungrypangproject.domain.membership.entity.GradeEnum;
import com.example.hungrypangproject.domain.membership.entity.Membership;
import com.example.hungrypangproject.domain.membership.entity.UserMembership;
import com.example.hungrypangproject.domain.membership.repository.MembershipRepository;
import com.example.hungrypangproject.domain.membership.repository.UserMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j(topic = "MembershipService")
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserMembershipRepository userMembershipRepository;

    /*
    * 1. 맴버십 생성 : 회원가입 시, 초기 셋팅 NORMAL (member에서 호출)
    * 2. 멤버십 등급 : 금액 합산 + 승급 조건 체크 + 등급 변경
    * 3. 멤버십 상태 변경 : 현재 등급 상태 조회, 누적액, 승급까지 남은 금액(계산) 조회
    */

    @Transactional
    public MembershipResponse setupMembership (Member member) {
        log.info("신규회원 멤버십 초기화: memberId {}", member.getMemberId());

        // NORMAL 등급 기본 셋팅
        Membership normalGrade = membershipRepository.findByGrade(GradeEnum.NORMAL)
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBERSHIP_NOT_GRADE));

        UserMembership userStatus = UserMembership.register(
                member,
                normalGrade
        );
        userMembershipRepository.save(userStatus);

        return MembershipResponse.register(userStatus, BigDecimal.ZERO);
    }

    public void calculateUpgrade(Member member, BigDecimal paymentAmount) {
        log.info("승급 등금 계산: memberId {}, 결제금액 {}", member.getMemberId(), paymentAmount);

        UserMembership userStatus = userMembershipRepository.findByMember(member)
                .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_USER_MEMBERSHIP));

        // 기존 등급 저장
        GradeEnum oldGrade = userStatus.getMembership().getGrade();

        // 누적액 합산 및 상태 업데이트
        BigDecimal upTotalAmount = userStatus.getTotalPrice().add(paymentAmount);

        // 새로운 누적액에 맞는 등급 조회
        GradeEnum targetGrade = GradeEnum.determineGrade(upTotalAmount);

        // 판별 등급을 사용해 엔티티 조회
        Membership upGrade = membershipRepository.findByGrade(targetGrade)
                .orElseThrow(()->new ServiceException(ErrorCode.MEMBERSHIP_NOT_GRADE));

        // 상태 변경
        userStatus.updateStatus(paymentAmount, upGrade);
        userMembershipRepository.save(userStatus);

        if(!oldGrade.equals(upGrade.getGrade())) {
            log.info("승급 알림: memberId {} | 기존 {} -> 승급 {}", member.getMemberId(), oldGrade, upGrade.getGrade());
        }
    }

    @Transactional(readOnly = true)
        public MembershipResponse getMembership (Member member) {
            UserMembership userStatus = userMembershipRepository.findByMember(member)
                    .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND_USER_MEMBERSHIP));

            // 다음 등급까지 남은 금액 계산
        Optional<Membership> nextGrade = membershipRepository.findByGrade(GradeEnum.NORMAL);
        BigDecimal remainAmount = nextGrade
                .map(m -> m.getMinTotalPaidAmount().subtract(userStatus.getTotalPrice()))
                .orElse(BigDecimal.ZERO); // 다음 등급이 없으면 0

        log.info("멤버쉽 상태 조회 완료: memberId {}, currentGrade {}", member.getMemberId(), userStatus.getMembership().getGrade());

        return MembershipResponse.register(userStatus, remainAmount);
    }
}
