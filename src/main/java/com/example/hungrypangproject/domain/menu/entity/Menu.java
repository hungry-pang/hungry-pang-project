package com.example.hungrypangproject.domain.menu.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.menu.exception.MenuException;
import com.example.hungrypangproject.domain.store.entity.Store;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "store_id")
    private Store store;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Long stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuStatus status;

    public static Menu create(
            Store store,
            String name,
            BigDecimal price,
            Long stock,
            MenuStatus status
    ) {
        Menu menu = new Menu();
        menu.store = store;
        menu.name = name;
        menu.price = price;
        menu.stock = stock;
        menu.status = status;
        return menu;
    }

    // 메뉴 수정
    public void update(String name, BigDecimal price, Long stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // 메뉴 상태 변경
    public void updateStatus(MenuStatus status) {
        this.status = status;
    }

    // 재고 차감
    public void decreaseStock(Long quantity) {
        if (this.stock < quantity) {
            throw new MenuException(ErrorCode.MENU_SOLD_OUT);
        }
        this.stock -= quantity;
        if (this.stock == 0) {
            this.status = MenuStatus.SOLDOUT;
        }
    }
}
