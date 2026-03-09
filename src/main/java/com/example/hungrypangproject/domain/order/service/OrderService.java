package com.example.hungrypangproject.domain.order.service;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import com.example.hungrypangproject.domain.menu.repository.MenuRepository;
import com.example.hungrypangproject.domain.order.dto.request.CreateOrderRequest;
import com.example.hungrypangproject.domain.order.dto.request.OrderItemRequest;
import com.example.hungrypangproject.domain.order.dto.response.CreateOrderResponse;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderItem;
import com.example.hungrypangproject.domain.order.repository.OrderItemRepository;
import com.example.hungrypangproject.domain.order.repository.OrderRepostory;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.entity.StoreStatus;
import com.example.hungrypangproject.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepostory orderRepostory;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public CreateOrderResponse save(Long userId, CreateOrderRequest request) {
        Store store = storeRepository.findById(request.getStoreId()).orElseThrow(
                () -> new IllegalStateException("매장 없음")
        );
        if (store.getStatus() != StoreStatus.OPEN) {
            throw new IllegalStateException("영업 중인 가게가 아님");
        }

        Map<Long, Long> menuIdToStock = request.getItems().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getMenuId, //key
                        OrderItemRequest::getStock  //value
                ));

        // key값만 뽑아내서 id목록으로 한번에 조회, 쿼리가 한번만 나감
        List<Menu> menus = menuRepository.findAllById(menuIdToStock.keySet());

        //요청한 메뉴와 실제 조회된 메뉴의 수가 다르면 에러(없는 메뉴를 호출 할때)
        if (menus.size() != request.getItems().size()) {
            throw new IllegalArgumentException("존재하지 않는 메뉴가 포함되어 있습니다.");
        }

        //품절 유무 확인
        for (Menu menu : menus) {
            if (menu.getStatus() == MenuStatus.SOLDOUT) {
                throw new IllegalArgumentException(menu.getName() + "은 품절된 메뉴입니다.");
            }
        }

        BigDecimal totalPrice = new BigDecimal(0);
        for (Menu menu : menus) {
            Long stock = menuIdToStock.get(menu.getId());// 수량
            totalPrice = totalPrice.add(menu.getPrice().multiply(new BigDecimal(stock)));//가격 계산
        }

        //최소 주문
        if(store.getMinimumOrder() != null && totalPrice.compareTo(store.getMinimumOrder()) < 0) {
            throw new IllegalStateException("최소 주문금액은" + store.getMinimumOrder()+ "원 입니다.");
        }

        //배달료 추가
        if(store.getDeliveryFee() != null){
            totalPrice = totalPrice.add(store.getDeliveryFee());
        }
        //포인트 사용 부분 멤버 엔티티 추가
        if (request.getUsedPoint() != null && request.getUsedPoint().compareTo(BigDecimal.ZERO) > 0) {
            Member findMember = memberRepository.findById(userId).orElseThrow(
                    () -> new IllegalStateException("회원을 찾을수없음")
            );
            if (new BigDecimal(findMember.getPoint()).compareTo(request.getUsedPoint()) < 0) {// compareTo 앞 < 뒷 -> -1 반환, 앞 == 뒤 -> 0 반환, 앞 > 뒤 -> 1반환
                throw new IllegalStateException("포인트가 부족합니다.");
            }

            BigDecimal maxUsePoint = totalPrice.multiply(new BigDecimal("0.1"));
            if (request.getUsedPoint().compareTo(maxUsePoint) > 0) {
                throw new IllegalStateException("포인트는 결제금액의 10% 이하만 사용가능");
            }
            totalPrice = totalPrice.subtract(request.getUsedPoint());
        }

        Member member = memberRepository.getReferenceById(userId);
        Order order = Order.create(totalPrice, request.getUsedPoint(), member, store);
        orderRepostory.save(order);

        // 주문 상품 저장
        List<OrderItem> orderItems = new ArrayList<>();
        for (Menu menu : menus) {
            Long stock = menuIdToStock.get(menu.getId());
            OrderItem orderItem = OrderItem.create(order, menu, stock);
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
        return CreateOrderResponse.from(order, orderItems);
    }

    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepostory.findById(orderId).orElseThrow(
                () -> new IllegalArgumentException("주문없음")
        );
        order.cancel(userId);
    }
}
