package com.example.hungrypangproject.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestDataController {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    @PostMapping("/orders")
    public String insertOrders() {
        int batchSize = 50000;
        int total = 5000000;

        for (int i = 0; i < total; i += batchSize) {
            List<Object[]> batch = new ArrayList<>();
            for (int j = 0; j < batchSize; j++) {
                batch.add(new Object[]{
                        10000 + random.nextInt(90000),                        // total_price
                        "COMPLETED",                                           // order_status
                        LocalDateTime.now().minusDays(random.nextInt(365)),   // order_at
                        0,                                                     // used_point
                        1,                                                     // user_id
                        1                                                      // store_id
                });
            }
            jdbcTemplate.batchUpdate(
                    "INSERT INTO orders (order_num, total_price, order_status, order_at, used_point, user_id, store_id) " +
                            "VALUES (UUID_TO_BIN(UUID()), ?, ?, ?, ?, ?, ?)",
                    batch
            );
            System.out.println((i + batchSize) + " / " + total + " 완료");
        }
        return "500만 건 삽입 완료";
    }
}