package com.example.hungrypangproject.domain.order.repository;

import com.example.hungrypangproject.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
