package com.example.hungrypangproject.domain.order.repository;

import com.example.hungrypangproject.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepostory extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "LEFT JOIN FETCH o.store " +
            "WHERE o.member.id = :memberId " +
            "ORDER BY o.orderAt DESC")
    List<Order> findAllByMemberIdWithItems(@Param("memberId") Long memberId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}
