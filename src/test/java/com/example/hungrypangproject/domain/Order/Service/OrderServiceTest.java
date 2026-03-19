package com.example.hungrypangproject.domain.order.service;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.membership.service.MembershipService;
import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import com.example.hungrypangproject.domain.menu.exception.MenuException;
import com.example.hungrypangproject.domain.menu.repository.MenuRepository;
import com.example.hungrypangproject.domain.order.dto.request.CreateOrderRequest;
import com.example.hungrypangproject.domain.order.dto.request.OrderItemRequest;
import com.example.hungrypangproject.domain.order.dto.request.UpdateOrderStatusRequest;
import com.example.hungrypangproject.domain.order.dto.response.CreateOrderResponse;
import com.example.hungrypangproject.domain.order.dto.response.OrderListResponse;
import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.order.entity.OrderItem;
import com.example.hungrypangproject.domain.order.entity.OrderStatus;
import com.example.hungrypangproject.domain.order.exception.OrderException;
import com.example.hungrypangproject.domain.order.repository.OrderItemRepository;
import com.example.hungrypangproject.domain.order.repository.OrderRepository;
import com.example.hungrypangproject.domain.point.repository.PointRepository;
import com.example.hungrypangproject.domain.point.service.PointService;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.entity.StoreStatus;
import com.example.hungrypangproject.domain.store.exception.StoreException;
import com.example.hungrypangproject.domain.store.repository.StoreRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock private MembershipService membershipService;
    @Mock private PointService pointService;
    @Mock private PointRepository pointRepository;

    private Member member;
    private Store store;
    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() {
        member = mock(Member.class);
        given(member.getMemberId()).willReturn(1L);
        given(member.getTotalPoint()).willReturn(BigDecimal.valueOf(0));

        store = mock(Store.class);
        given(store.getStatus()).willReturn(StoreStatus.OPEN);
        given(store.getMinimumOrder()).willReturn(BigDecimal.valueOf(10000));
        given(store.getDeliveryFee()).willReturn(BigDecimal.valueOf(3000));

        menu1 = mock(Menu.class);
        given(menu1.getId()).willReturn(1L);
        given(menu1.getName()).willReturn("후라이드치킨");
        given(menu1.getPrice()).willReturn(BigDecimal.valueOf(18000));
        given(menu1.getStatus()).willReturn(MenuStatus.SALE);

        menu2 = mock(Menu.class);
        given(menu2.getId()).willReturn(2L);
        given(menu2.getName()).willReturn("양념치킨");
        given(menu2.getPrice()).willReturn(BigDecimal.valueOf(19000));
        given(menu2.getStatus()).willReturn(MenuStatus.SALE);
    }

    @Nested
    @DisplayName("주문 생성")
    class SaveOrder {

        @Test
        @DisplayName("정상적으로 주문이 생성된다")
        void save_success() {
            // given
            CreateOrderRequest request = mock(CreateOrderRequest.class);
            OrderItemRequest item1 = mock(OrderItemRequest.class);
            OrderItemRequest item2 = mock(OrderItemRequest.class);

            given(item1.getMenuId()).willReturn(1L);
            given(item1.getStock()).willReturn(1L);
            given(item2.getMenuId()).willReturn(2L);
            given(item2.getStock()).willReturn(1L);

            given(request.getStoreId()).willReturn(1L);
            given(request.getItems()).willReturn(List.of(item1, item2));
            given(request.getUsedPoint()).willReturn(BigDecimal.ZERO);

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(menuRepository.findAllByIdInAndStoreId(anyList(), eq(1L))) .willReturn(List.of(menu1, menu2));
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));


            Order savedOrder = mock(Order.class);
            given(savedOrder.getId()).willReturn(1L);
            given(savedOrder.getOrderNum()).willReturn(UUID.randomUUID());
            given(orderRepository.save(any())).willReturn(savedOrder);
            given(orderItemRepository.saveAll(anyList())).willReturn(List.of());
            given(savedOrder.getFinalPaymentAmount()).willReturn(new BigDecimal("21000"));
            given(pointService.calculateEarnedPoints(any(), any())).willReturn(BigDecimal.ZERO);

            // when
            CreateOrderResponse response = orderService.save(1L, request);

            // then
            assertThat(response).isNotNull();
            verify(orderRepository).save(any(Order.class));
            verify(orderItemRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("영업 중이 아닌 가게에 주문하면 예외가 발생한다")
        void save_storeNotOpen() {
            // given
            given(store.getStatus()).willReturn(StoreStatus.CLOSED);
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));

            CreateOrderRequest request = mock(CreateOrderRequest.class);
            given(request.getStoreId()).willReturn(1L);

            // when & then
            assertThatThrownBy(() -> orderService.save(1L, request))
                    .isInstanceOf(StoreException.class);
        }

        @Test
        @DisplayName("존재하지 않는 메뉴를 주문하면 예외가 발생한다")
        void save_menuNotFound() {
            // given
            CreateOrderRequest request = mock(CreateOrderRequest.class);
            OrderItemRequest item1 = mock(OrderItemRequest.class);
            OrderItemRequest item2 = mock(OrderItemRequest.class);

            given(item1.getMenuId()).willReturn(1L);
            given(item1.getStock()).willReturn(1L);
            given(item2.getMenuId()).willReturn(999L); // 존재하지 않는 메뉴
            given(item2.getStock()).willReturn(1L);

            given(request.getStoreId()).willReturn(1L);
            given(request.getItems()).willReturn(List.of(item1, item2));

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(menuRepository.findAllByIdInAndStoreId(anyList(), eq(1L))) .willReturn(List.of(menu1));// 1개만 반환

            // when & then
            assertThatThrownBy(() -> orderService.save(1L, request))
                    .isInstanceOf(MenuException.class);
        }

        @Test
        @DisplayName("품절된 메뉴를 주문하면 예외가 발생한다")
        void save_menuSoldOut() {
            // given
            given(menu1.getStatus()).willReturn(MenuStatus.SOLDOUT);

            CreateOrderRequest request = mock(CreateOrderRequest.class);
            OrderItemRequest item1 = mock(OrderItemRequest.class);
            given(item1.getMenuId()).willReturn(1L);
            given(item1.getStock()).willReturn(1L);

            given(request.getStoreId()).willReturn(1L);
            given(request.getItems()).willReturn(List.of(item1));

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(menuRepository.findAllByIdInAndStoreId(anyList(), eq(1L))) .willReturn(List.of(menu1));

            // when & then
            assertThatThrownBy(() -> orderService.save(1L, request))
                    .isInstanceOf(MenuException.class);
        }

        @Test
        @DisplayName("최소 주문 금액 미달 시 예외가 발생한다")
        void save_belowMinimumOrder() {
            // given
            given(store.getMinimumOrder()).willReturn(BigDecimal.valueOf(50000));

            CreateOrderRequest request = mock(CreateOrderRequest.class);
            OrderItemRequest item1 = mock(OrderItemRequest.class);
            given(item1.getMenuId()).willReturn(1L);
            given(item1.getStock()).willReturn(1L);

            given(request.getStoreId()).willReturn(1L);
            given(request.getItems()).willReturn(List.of(item1));
            given(request.getUsedPoint()).willReturn(BigDecimal.ZERO);

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(menuRepository.findAllByIdInAndStoreId(anyList(), eq(1L))) .willReturn(List.of(menu1));

            // when & then
            assertThatThrownBy(() -> orderService.save(1L, request))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class CancelOrder {

        @Test
        @DisplayName("본인 주문을 정상적으로 취소한다")
        void cancelOrder_success() {
            // given
            Order order = mock(Order.class);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            // when
            orderService.cancelOrder(1L, 1L);

            // then
            verify(order).cancel(1L);
        }

        @Test
        @DisplayName("존재하지 않는 주문 취소 시 예외가 발생한다")
        void cancelOrder_notFound() {
            // given
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(1L, 999L))
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("본인의 주문 목록을 정상적으로 조회한다")
        void getOrders_success() {
            // given
            Order order = mock(Order.class);
            OrderItem orderItem = mock(OrderItem.class);
            given(orderItem.getName()).willReturn("후라이드치킨");
            given(order.getOrderItems()).willReturn(List.of(orderItem));
            given(order.getStore()).willReturn(store);
            given(store.getStoreName()).willReturn("테스트식당");
            given(order.getTotalPrice()).willReturn(BigDecimal.valueOf(21000));
            given(order.getOrderStatus()).willReturn(OrderStatus.WAITING);
            given(order.getOrderAt()).willReturn(null);

            given(orderRepository.findAllByMemberIdWithItems(1L)).willReturn(List.of(order));

            // when
            List<OrderListResponse> result = orderService.getOrders(1L);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("주문이 없으면 빈 리스트를 반환한다")
        void getOrders_empty() {
            // given
            given(orderRepository.findAllByMemberIdWithItems(1L)).willReturn(List.of());

            // when
            List<OrderListResponse> result = orderService.getOrders(1L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class UpdateOrderStatus {

        @Test
        @DisplayName("가게 주인이 주문 상태를 정상적으로 변경한다")
        void updateOrderStatus_success() {
            // given
            Order order = mock(Order.class);
            given(order.getStore()).willReturn(store);
            given(store.isOwner(1L)).willReturn(true);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            UpdateOrderStatusRequest request = mock(UpdateOrderStatusRequest.class);
            given(request.getOrderStatus()).willReturn(OrderStatus.PREPARING);

            // when
            orderService.updateOrderStatus(1L, 1L, request);

            // then
            verify(order).updateStatus(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("본인 가게 주문이 아니면 예외가 발생한다")
        void updateOrderStatus_forbidden() {
            // given
            Order order = mock(Order.class);
            given(order.getStore()).willReturn(store);
            given(store.isOwner(2L)).willReturn(false);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            UpdateOrderStatusRequest request = mock(UpdateOrderStatusRequest.class);

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(2L, 1L, request))
                    .isInstanceOf(OrderException.class);
        }
    }
}