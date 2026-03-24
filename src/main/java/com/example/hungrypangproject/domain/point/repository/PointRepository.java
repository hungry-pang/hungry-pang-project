package com.example.hungrypangproject.domain.point.repository;

import com.example.hungrypangproject.domain.order.entity.Order;
import com.example.hungrypangproject.domain.point.entity.Point;
import com.example.hungrypangproject.domain.point.entity.PointEnum;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PointRepository extends JpaRepository <Point, Long> {

    @EntityGraph(attributePaths = {"member"})
    Optional<Point> findFirstByOrderAndStatusOrderByCreatedAtDesc(Order order, PointEnum status);

    Boolean existsByOrderAndStatus (Order order, PointEnum status);
}
