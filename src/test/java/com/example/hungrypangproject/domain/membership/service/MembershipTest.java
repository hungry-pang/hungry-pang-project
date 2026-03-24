package com.example.hungrypangproject.domain.membership.service;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.membership.entity.GradeEnum;
import com.example.hungrypangproject.domain.membership.entity.Membership;
import com.example.hungrypangproject.domain.membership.entity.UserMembership;
import com.example.hungrypangproject.domain.membership.repository.MembershipRepository;
import com.example.hungrypangproject.domain.membership.repository.UserMembershipRepository;
import com.example.hungrypangproject.domain.order.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class MembershipTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserMembershipRepository userMembershipRepository;

    @InjectMocks
    private MembershipService membershipService;

    private Member member;
    private Order order;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .email("test@test.com")
                .build();
        order = Order.builder()
                .id(1L)
                .orderNum(UUID.fromString(UUID.randomUUID().toString()))
                .totalPrice(BigDecimal.valueOf(10000))
                .build();
    }

    @Test
    @DisplayName("결제 후 누적액 증가 및 등급 유지 또는 승급")
    void calculateUpgrade_Success() {

        // given
        BigDecimal paymentAmount = order.getTotalPrice();

        Membership normalGrade = Membership
                .builder()
                .grade(GradeEnum.NORMAL)
                .build();

        UserMembership userStatus = UserMembership
                .builder()
                .member(member)
                .membership(normalGrade)
                .totalPrice(BigDecimal.ZERO)
                .build();

        given(userMembershipRepository.findByMember(member)).willReturn(Optional.of(userStatus));

        given(membershipRepository.findByGrade(any(GradeEnum.class))).willReturn(Optional.of(normalGrade));

        // when
        membershipService.calculateUpgrade(member, paymentAmount);

        // then
        assertThat(userStatus.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(userStatus.getMembership().getGrade()).isEqualTo(GradeEnum.NORMAL);
        verify(userMembershipRepository, times(1)).save(any(UserMembership.class));

    }

    @Test
    @DisplayName("결제 후 누적액이 10만원이 넘으면 VIP 로 변경되어야 한다")
    void calculateUpgrade_To_VIP() {
        // given
        BigDecimal payment = BigDecimal.valueOf(101000);

        Membership normalGrade = Membership
                .builder()
                .grade(GradeEnum.NORMAL)
                .build();

        Membership vipGrade = Membership
                .builder()
                .grade(GradeEnum.VIP)
                .build();

        UserMembership userStatus = UserMembership
                .builder()
                .membership(normalGrade)
                .totalPrice(BigDecimal.ZERO)
                .build();

        given(userMembershipRepository.findByMember(member)).willReturn(Optional.of(userStatus));
        given(membershipRepository.findByGrade(GradeEnum.VIP)).willReturn(Optional.of(vipGrade));

        // when
        membershipService.calculateUpgrade(member, payment);

        //then
        assertThat(userStatus.getTotalPrice()).isEqualByComparingTo(payment);
        assertThat(userStatus.getMembership().getGrade()).isEqualTo(GradeEnum.VIP);
        verify(userMembershipRepository).save(any(UserMembership.class));
    }

    @Test
    @DisplayName("누적액이 30만원 이상이 될 시 vvip 등급으로 승급되어야 한다.")
    void calculateUpgrade_To_VVIP() {
        // given
        BigDecimal currentTotal = BigDecimal.valueOf(290000);
        BigDecimal newPayment = BigDecimal.valueOf(11000);

        Membership vipGrade = Membership
                .builder()
                .grade(GradeEnum.VIP)
                .build();

        Membership vvipGrade = Membership
                .builder()
                .grade(GradeEnum.VVIP)
                .build();

        UserMembership userStatus = UserMembership
                .builder()
                .membership(vipGrade)
                .totalPrice(currentTotal)
                .build();

        given(userMembershipRepository.findByMember(member)).willReturn(Optional.of(userStatus));
        given(membershipRepository.findByGrade(GradeEnum.VVIP)).willReturn(Optional.of(vvipGrade));

        // when
        membershipService.calculateUpgrade(member, newPayment);

        //then
        assertThat(userStatus.getMembership().getGrade()).isEqualTo(GradeEnum.VVIP);
        verify(userMembershipRepository).save(any(UserMembership.class));
    }
}
