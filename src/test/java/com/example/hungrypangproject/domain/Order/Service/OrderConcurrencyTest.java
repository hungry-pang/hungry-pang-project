package com.example.hungrypangproject.domain.order.service;

import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import com.example.hungrypangproject.domain.menu.repository.MenuRepository;
import com.example.hungrypangproject.domain.order.dto.request.CreateOrderRequest;
import com.example.hungrypangproject.domain.order.dto.request.OrderItemRequest;
import com.example.hungrypangproject.domain.membership.service.MembershipService;
import com.example.hungrypangproject.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuRepository menuRepository;

    // 재고 동시성만 검증하기 위해 부가 로직 mock 처리
    @MockitoBean
    private PointService pointService;

    @MockitoBean
    private MembershipService membershipService;

    //Docker로 Mysql 컨테이너 사용
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Test
    @DisplayName("재고 1개 메뉴를 10명이 동시 주문 시 1명만 성공한다")
    void concurrentOrder_onlyOneSucceeds() throws InterruptedException {
        // given
        int threadCount = 10;
        long userId = 1L;
        long storeId = 1L;
        long menuId = 1L;

        Menu menu = menuRepository.findById(menuId).orElseThrow();
        ReflectionTestUtils.setField(menu, "stock", 1L);
        ReflectionTestUtils.setField(menu, "status", MenuStatus.SALE);
        menuRepository.saveAndFlush(menu);

        CreateOrderRequest request = makeRequest(storeId, menuId, 1L);

        //동시 요청
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    orderService.save(userId, request);
                    successCount.incrementAndGet();

                } catch (Throwable t) {
                    failCount.incrementAndGet();
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        Menu updatedMenu = menuRepository.findById(menuId).orElseThrow();

        System.out.println("재고 1개 테스트");
        System.out.println(" 성공: " + successCount.get() + "건");
        System.out.println(" 실패: " + failCount.get() + "건");
        System.out.println(" 남은 재고: " + updatedMenu.getStock());

        if (!errors.isEmpty()) {
            System.out.println("실패 예외 목록");
            errors.forEach(Throwable::printStackTrace);
        }

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(updatedMenu.getStock()).isEqualTo(0L);
        assertThat(updatedMenu.getStock()).isGreaterThanOrEqualTo(0L);
        assertThat(updatedMenu.getStatus()).isEqualTo(MenuStatus.SOLDOUT);
    }

    @Test
    @DisplayName("재고 5개 메뉴를 10명이 동시 주문 시 5명만 성공한다")
    void concurrentOrder_fiveSucceeds() throws InterruptedException {
        // given
        int threadCount = 10;
        long initialStock = 5L;
        long userId = 1L;
        long storeId = 1L;
        long menuId = 1L;

        Menu menu = menuRepository.findById(menuId).orElseThrow();
        ReflectionTestUtils.setField(menu, "stock", initialStock);
        ReflectionTestUtils.setField(menu, "status", MenuStatus.SALE);
        menuRepository.saveAndFlush(menu);

        CreateOrderRequest request = makeRequest(storeId, menuId, 1L);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    orderService.save(userId, request);
                    successCount.incrementAndGet();

                } catch (Throwable t) {
                    failCount.incrementAndGet();
                    errors.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // then
        Menu updatedMenu = menuRepository.findById(menuId).orElseThrow();

        System.out.println("재고 5개 테스트");
        System.out.println(" 성공: " + successCount.get() + "건");
        System.out.println(" 실패: " + failCount.get() + "건");
        System.out.println(" 남은 재고: " + updatedMenu.getStock());

        if (!errors.isEmpty()) {
            System.out.println("실패 예외 목록");
            errors.forEach(Throwable::printStackTrace);
        }

        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failCount.get()).isEqualTo(threadCount - 5);
        assertThat(updatedMenu.getStock()).isEqualTo(0L);
        assertThat(updatedMenu.getStock()).isGreaterThanOrEqualTo(0L);
        assertThat(updatedMenu.getStatus()).isEqualTo(MenuStatus.SOLDOUT);
    }

    private CreateOrderRequest makeRequest(Long storeId, Long menuId, Long stock) {
        OrderItemRequest item = new OrderItemRequest();
        ReflectionTestUtils.setField(item, "menuId", menuId);
        ReflectionTestUtils.setField(item, "stock", stock);

        CreateOrderRequest request = new CreateOrderRequest();
        ReflectionTestUtils.setField(request, "storeId", storeId);
        ReflectionTestUtils.setField(request, "items", List.of(item));
        ReflectionTestUtils.setField(request, "usedPoint", null);
        return request;
    }
}