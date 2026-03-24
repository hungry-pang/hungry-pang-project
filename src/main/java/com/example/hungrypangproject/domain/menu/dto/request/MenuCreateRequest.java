package com.example.hungrypangproject.domain.menu.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class MenuCreateRequest {

    private String name;
    private BigDecimal price;
    private Long stock;
}
