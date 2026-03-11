package com.example.hungrypangproject.domain.delivery.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.delivery.dto.request.CreateDeliveryRequest;
import com.example.hungrypangproject.domain.delivery.dto.response.CreateDeliveryResponse;
import com.example.hungrypangproject.domain.delivery.dto.response.DeliveryDetailResponse;
import com.example.hungrypangproject.domain.delivery.entity.Delivery;
import com.example.hungrypangproject.domain.delivery.exception.DeliveryException;
import com.example.hungrypangproject.domain.delivery.repository.DeliveryRepository;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreateDeliveryResponse createDelivery(CreateDeliveryRequest request){

        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new DeliveryException(ErrorCode.ORDER_NOT_FOUND)
        );

        // 이미 배달 요청이 있는지 확인
        if (deliveryRepository.existsByOrderId(order.getId())) {
            throw new DeliveryException(ErrorCode.DELIVERY_ALREADY_EXISTS);
        }

        // RAIDER 목록 조회 후 랜덤 배정
        List<Member> raiders = memberRepository.findAllByRole(MemberRoleEnum.ROLE_RAIDER);
        if (raiders.isEmpty()) {
            throw new DeliveryException(ErrorCode.RAIDER_NOT_FOUND);
        }
        Member raider = raiders.get(new Random().nextInt(raiders.size()));

        // PENDING으로 생성
        Delivery delivery = Delivery.create(
                raider.getMemberId(),
                request.getDeliveryAddress(),
                request.getDeliveryFee(),
                order
        );
        deliveryRepository.save(delivery);

        // 즉시 DELIVERING으로 전환 + pickupAt 저장
        delivery.startDelivering();

        return CreateDeliveryResponse.from(delivery, raider.getNickname());
    }

    // 라이더 - 배달 완료
    @Transactional
    public void completeDelivery(Long deliveryId, Long riderId) {
        Delivery delivery = deliveryRepository.findByIdWithDetails(deliveryId).orElseThrow(
                () -> new DeliveryException(ErrorCode.DELIVERY_NOT_FOUND)
        );
        delivery.complete(riderId);
    }

    // 유저 본인 주문의 배달 상태 조회
    @Transactional(readOnly = true)
    public DeliveryDetailResponse getDeliveryByOrder(Long orderId, Long userId) {
        Delivery delivery = deliveryRepository.findByOrderIdWithDetails(orderId).orElseThrow(
                () -> new DeliveryException(ErrorCode.DELIVERY_NOT_FOUND)
        );

        // 본인 주문인지 확인
        if (!delivery.getOrder().getMember().getMemberId().equals(userId)) {
            throw new DeliveryException(ErrorCode.DELIVERY_FORBIDDEN);
        }

        return DeliveryDetailResponse.from(delivery);
    }

}
