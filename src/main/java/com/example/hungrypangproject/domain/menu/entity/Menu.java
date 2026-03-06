package com.example.hungrypangproject.domain.menu.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
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

    @JoinColumn(name = "store_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
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

    public Menu(Store store, String name, BigDecimal price, Long stock, MenuStatus status) {
        this.store = store;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public void update(String name, BigDecimal price, Long stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void updateStatus(MenuStatus status) {
        this.status = status;
    }
}
