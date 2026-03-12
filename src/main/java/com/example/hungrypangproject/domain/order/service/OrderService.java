package com.example.hungrypangproject.domain.order.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.exception.MemberException;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import com.example.hungrypangproject.domain.menu.exception.MenuException;
import com.example.hungrypangproject.domain.menu.repository.MenuRepository;
import com.example.hungrypangproject.domain.order.dto.request.CreateOrderRequest;
import com.example.hungrypangproject.domain.order.dto.request.OrderItemRequest;
import com.example.hungrypangproject.domain.order.dto.request.UpdateOrderStatusRequest;
import com.example.hungrypangproject.domain.order.dto.response.CreateOrderResponse;
import com.example.hungrypangproject.domain.order.dto.response.OrderDetailResponse;
import com.example.hungrypangproject.domain.order.dto.response.OrderListResponse;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderItem;
import com.example.hungrypangproject.domain.order.exception.OrderException;
import com.example.hungrypangproject.domain.order.repository.OrderItemRepository;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import com.example.hungrypangproject.domain.point.exception.PointException;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.entity.StoreStatus;
import com.example.hungrypangproject.domain.store.exception.StoreException;
import com.example.hungrypangproject.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final OrderItemRepository orderItemRepository;


    @Transactional
    public CreateOrderResponse save(Long userId, CreateOrderRequest request) {
        Store store = storeRepository.findById(request.getStoreId()).orElseThrow(
                () -> new StoreException(ErrorCode.STORE_NOT_FOUND)
        );
        if (store.getStatus() != StoreStatus.OPEN) {
            throw new StoreException(ErrorCode.STORE_NOT_OPEN);
        }

        Map<Long, Long> menuIdToStock = request.getItems().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getMenuId, //key
                        OrderItemRequest::getStock  //value
                ));

        List<Long> menuIds = new ArrayList<>(menuIdToStock.keySet());
        // key값만 뽑아내서 id목록으로 한번에 조회, 쿼리가 한번만 나감4
        List<Menu> menus = menuRepository.findAllById(menuIds);




        if (menus.size() != request.getItems().size()) {
            throw new MenuException(ErrorCode.MENU_NOT_FOUND);
        }

        if (menuRepository.existsByIdInAndStatus(menuIds, MenuStatus.SOLDOUT)) {
            throw new MenuException(ErrorCode.MENU_SOLD_OUT);
        }


            BigDecimal totalPrice = BigDecimal.ZERO;
            for (Menu menu : menus) {
                Long stock = menuIdToStock.get(menu.getId());// 수량
                totalPrice = totalPrice.add(menu.getPrice().multiply(new BigDecimal(stock)));//가격 계산
            }

            //최소 주문
            if (store.getMinimumOrder() != null && totalPrice.compareTo(store.getMinimumOrder()) < 0) {
                throw new OrderException(ErrorCode.ORDER_BELOW_MINIMUM);
        }

        //배달료 추가
        if(store.getDeliveryFee() != null){
            totalPrice = totalPrice.add(store.getDeliveryFee());
        }
        Member member = memberRepository.findById(userId).orElseThrow(
                () -> new MemberException(ErrorCode.MEMBER_NOT_FOUND)
        );
        //포인트 사용 부분 멤버 엔티티 추가
        if (request.getUsedPoint() != null && request.getUsedPoint().compareTo(BigDecimal.ZERO) > 0) {

            if (new BigDecimal(member.getTotalPoint()).compareTo(request.getUsedPoint()) < 0) {// compareTo 앞 < 뒷 -> -1 반환, 앞 == 뒤 -> 0 반환, 앞 > 뒤 -> 1반환
                throw new PointException(ErrorCode.POINT_NOT_ENOUGH);
            }

            //포인트는 전체 금액의 10% 이상 사용될 수 없음
            BigDecimal maxUsePoint = totalPrice.multiply(new BigDecimal("0.1"));
            if (request.getUsedPoint().compareTo(maxUsePoint) > 0) {
                throw new PointException(ErrorCode.POINT_EXCEED_LIMIT);
            }
            totalPrice = totalPrice.subtract(request.getUsedPoint());
        }

        Order order = Order.create(totalPrice, request.getUsedPoint(), member, store);
        Order saveOrder = orderRepository.save(order);

        // 주문 상품 저장
        List<OrderItem> orderItems = new ArrayList<>();
        for (Menu menu : menus) {
            Long stock = menuIdToStock.get(menu.getId());
            menu.decreaseStock(stock);
            OrderItem orderItem = OrderItem.create(saveOrder, menu, stock);
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
        return CreateOrderResponse.from(saveOrder, orderItems);
    }

    //주문 취소
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderException(ErrorCode.ORDER_NOT_FOUND)
        );
        order.cancel(userId);
    }

    //주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrders(Long userId) {
        List<Order> orders = orderRepository.findAllByMemberIdWithItems(userId);
        return orders.stream()
                .map(OrderListResponse::from)
                .toList();
    }

    //주문 단건 조회
    @Transactional(readOnly = true)
    public OrderDetailResponse getOneOrder(Long userId, Long orderId) {
       Order order = orderRepository.findByIdWithItems(orderId).orElseThrow(
               () -> new OrderException(ErrorCode.ORDER_NOT_FOUND)
       );
       if(!order.getMember().getMemberId().equals(userId)){
           throw new OrderException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
       }
       return OrderDetailResponse.from(order, order.getOrderItems());

    }

    @Transactional
    public void updateOrderStatus(Long storeOwnerId, Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderException(ErrorCode.ORDER_NOT_FOUND)
        );
        // 본인 가게 주문인지 확인(인증 인가 구현시 수정)
        if (!order.getStore().isOwner(storeOwnerId)) {
            throw new OrderException(ErrorCode.ORDER_CANCEL_FORBIDDEN);
        }
        order.updateStatus(request.getOrderStatus());
    }

}
