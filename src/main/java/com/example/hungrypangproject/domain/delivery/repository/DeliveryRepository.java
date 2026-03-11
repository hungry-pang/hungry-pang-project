package com.example.hungrypangproject.domain.delivery.repository;

import com.example.hungrypangproject.domain.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {

    @Query("SELECT d FROM Delivery d " +
            "LEFT JOIN FETCH d.order o " +
            "LEFT JOIN FETCH o.member " +
            "WHERE d.id = :deliveryId")
    Optional<Delivery> findByIdWithDetails(@Param("deliveryId") Long deliveryId);

    // 주문 기준 배달 조회 (유저가 자기 주문 배달 상태 확인)
    @Query("SELECT d FROM Delivery d " +
            "LEFT JOIN FETCH d.order o " +
            "LEFT JOIN FETCH o.member " +
            "WHERE d.order.id = :orderId")
    Optional<Delivery> findByOrderIdWithDetails(@Param("orderId") Long orderId);

    boolean existsByOrderId(Long orderId);
}
