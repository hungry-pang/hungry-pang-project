package com.example.hungrypangproject.domain.coupon.repository;

import com.example.hungrypangproject.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon,Long> {
}
