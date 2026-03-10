package com.example.hungrypangproject.domain.point.repository;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.point.entity.Point;
import com.example.hungrypangproject.domain.point.entity.PointEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository <Point, Long> {
    Optional<Point> findFirstByOrderAndStatusOrderByCreatedAtDesc(Order order, PointEnum status);
}
