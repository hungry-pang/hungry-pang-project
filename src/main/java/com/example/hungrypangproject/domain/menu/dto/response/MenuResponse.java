package com.example.hungrypangproject.domain.menu.dto.response;

import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MenuResponse {

    private Long id;
    private Long storeId;
    private String name;
    private BigDecimal price;
    private Long stock;
    private MenuStatus status;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .stock(menu.getStock())
                .status(menu.getStatus())
                .build();
    }
}
