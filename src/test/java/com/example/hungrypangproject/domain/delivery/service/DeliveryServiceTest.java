package com.example.hungrypangproject.domain.delivery.service;

import com.example.hungrypangproject.domain.delivery.dto.request.CreateDeliveryRequest;
import com.example.hungrypangproject.domain.delivery.dto.response.CreateDeliveryResponse;
import com.example.hungrypangproject.domain.delivery.dto.response.DeliveryDetailResponse;
import com.example.hungrypangproject.domain.delivery.entity.Delivery;
import com.example.hungrypangproject.domain.delivery.entity.DeliveryStatus;
import com.example.hungrypangproject.domain.delivery.exception.DeliveryException;
import com.example.hungrypangproject.domain.delivery.repository.DeliveryRepository;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeliveryServiceTest {

    @InjectMocks
    private DeliveryService deliveryService;

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MemberRepository memberRepository;

    private Member raider;
    private Order order;

    @BeforeEach
    void setUp() {
        raider = mock(Member.class);
        given(raider.getMemberId()).willReturn(1L);
        given(raider.getNickname()).willReturn("라이더1");

        order = mock(Order.class);
        given(order.getId()).willReturn(1L);
    }

    @Nested
    @DisplayName("배달 요청 생성")
    class CreateDelivery {

        @Test
        @DisplayName("배달 요청이 정상적으로 생성되고 즉시 DELIVERING 상태가 된다")
        void createDelivery_success() {
            // given
            CreateDeliveryRequest request = mock(CreateDeliveryRequest.class);
            given(request.getOrderId()).willReturn(1L);
            given(request.getDeliveryAddress()).willReturn("서울시 강남구");
            given(request.getDeliveryFee()).willReturn(BigDecimal.valueOf(3000));

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(deliveryRepository.existsByOrderId(1L)).willReturn(false);
            given(memberRepository.findAllByRole(MemberRoleEnum.ROLE_RAIDER)).willReturn(List.of(raider));

            Delivery savedDelivery = Delivery.create(1L, "서울시 강남구", BigDecimal.valueOf(3000), order);
            given(deliveryRepository.save(any(Delivery.class))).willReturn(savedDelivery);

            // when
            CreateDeliveryResponse response = deliveryService.createDelivery(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERING); // 상태로 검증
            verify(deliveryRepository).save(any(Delivery.class));
        }

        @Test
        @DisplayName("존재하지 않는 주문으로 배달 요청 시 예외가 발생한다")
        void createDelivery_orderNotFound() {
            // given
            CreateDeliveryRequest request = mock(CreateDeliveryRequest.class);
            given(request.getOrderId()).willReturn(999L);
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            // when & then - DeliveryService는 DeliveryException을 던짐
            assertThatThrownBy(() -> deliveryService.createDelivery(request))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("이미 배달 요청이 있는 주문에 중복 요청 시 예외가 발생한다")
        void createDelivery_alreadyExists() {
            // given
            CreateDeliveryRequest request = mock(CreateDeliveryRequest.class);
            given(request.getOrderId()).willReturn(1L);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(deliveryRepository.existsByOrderId(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> deliveryService.createDelivery(request))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("배정 가능한 라이더가 없으면 예외가 발생한다")
        void createDelivery_noRaider() {
            // given
            CreateDeliveryRequest request = mock(CreateDeliveryRequest.class);
            given(request.getOrderId()).willReturn(1L);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(deliveryRepository.existsByOrderId(1L)).willReturn(false);
            given(memberRepository.findAllByRole(MemberRoleEnum.ROLE_RAIDER)).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> deliveryService.createDelivery(request))
                    .isInstanceOf(DeliveryException.class);
        }
    }

    @Nested
    @DisplayName("배달 완료 처리")
    class CompleteDelivery {

        @Test
        @DisplayName("라이더가 배달을 정상적으로 완료한다")
        void completeDelivery_success() {
            // given
            Delivery delivery = mock(Delivery.class);
            given(deliveryRepository.findByIdWithDetails(1L)).willReturn(Optional.of(delivery));

            // when
            deliveryService.completeDelivery(1L, 1L);

            // then
            verify(delivery).complete(1L);
        }

        @Test
        @DisplayName("존재하지 않는 배달 완료 처리 시 예외가 발생한다")
        void completeDelivery_notFound() {
            // given
            given(deliveryRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> deliveryService.completeDelivery(999L, 1L))
                    .isInstanceOf(DeliveryException.class);
        }
    }

    @Nested
    @DisplayName("주문 배달 상태 조회")
    class GetDeliveryByOrder {

        @Test
        @DisplayName("본인 주문의 배달 상태를 정상적으로 조회한다")
        void getDeliveryByOrder_success() {
            // given
            Member orderMember = mock(Member.class);
            given(orderMember.getMemberId()).willReturn(1L);
            given(order.getMember()).willReturn(orderMember);

            Delivery delivery = mock(Delivery.class);
            given(delivery.getId()).willReturn(1L);
            given(delivery.getRiderId()).willReturn(1L);
            given(delivery.getOrder()).willReturn(order);
            given(delivery.getDeliveryStatus()).willReturn(DeliveryStatus.DELIVERING);
            given(delivery.getDeliveryAddress()).willReturn("서울시 강남구");
            given(delivery.getDeliveryFee()).willReturn(BigDecimal.valueOf(3000));
            given(delivery.getPickupAt()).willReturn(null);
            given(delivery.getDeliveryAt()).willReturn(null);

            given(deliveryRepository.findByOrderIdWithDetails(1L)).willReturn(Optional.of(delivery));

            // when
            DeliveryDetailResponse response = deliveryService.getDeliveryByOrder(1L, 1L);

            // then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("본인 주문이 아닌 배달 조회 시 예외가 발생한다")
        void getDeliveryByOrder_forbidden() {
            // given
            Member orderMember = mock(Member.class);
            given(orderMember.getMemberId()).willReturn(2L);
            given(order.getMember()).willReturn(orderMember);

            Delivery delivery = mock(Delivery.class);
            given(delivery.getOrder()).willReturn(order);
            given(deliveryRepository.findByOrderIdWithDetails(1L)).willReturn(Optional.of(delivery));

            // when & then
            assertThatThrownBy(() -> deliveryService.getDeliveryByOrder(1L, 1L))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문의 배달 조회 시 예외가 발생한다")
        void getDeliveryByOrder_notFound() {
            // given
            given(deliveryRepository.findByOrderIdWithDetails(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> deliveryService.getDeliveryByOrder(999L, 1L))
                    .isInstanceOf(DeliveryException.class);
        }
    }
}