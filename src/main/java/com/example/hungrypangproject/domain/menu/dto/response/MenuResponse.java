package com.example.hungrypangproject.domain.menu.dto.response;

import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MenuResponse {

    private Long id;
    private Long storeId;
    private String name;
    private BigDecimal price;
    private Long stock;
    private MenuStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .storeId(menu.getStore().getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .stock(menu.getStock())
                .status(menu.getStatus())
                .createdAt(menu.getCreatedAt())
                .modifiedAt(menu.getModifiedAt())
                .build();
    }
}
