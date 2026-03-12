package com.example.hungrypangproject.common.config;

import com.example.hungrypangproject.domain.membership.entity.GradeEnum;
import com.example.hungrypangproject.domain.membership.entity.Membership;
import com.example.hungrypangproject.domain.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /*
     * 회원가입 시, 초기 등급 설정 자동 셋팅 클래스
     */

    private final MembershipRepository membershipRepository;

    @Override
    public void run(String... args) {
        if (membershipRepository.count() == 0) {
            for (GradeEnum grade : GradeEnum.values()) {
                membershipRepository.save(Membership.register(
                        grade.getEarnRate(),
                        grade,
                        getMaxAmountForGrade(grade),
                        grade.getMinAmount(),
                        grade.getDescription()
                ));
            }
            log.info("멤버십 등급 정책이 Enum 기준으로 자동 생성되었습니다.");
        }
    }

    private BigDecimal getMaxAmountForGrade(GradeEnum grade) {
        return switch (grade) {
            case NORMAL -> GradeEnum.VIP.getMinAmount().subtract(BigDecimal.ONE);
            case VIP -> GradeEnum.VVIP.getMinAmount().subtract(BigDecimal.ONE);
            case VVIP -> new BigDecimal("999999999");
        };
    }
}