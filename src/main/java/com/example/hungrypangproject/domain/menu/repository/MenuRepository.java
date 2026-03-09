package com.example.hungrypangproject.domain.menu.repository;

import com.example.hungrypangproject.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByStoreIdOrderByCreatedAtDesc(Long storeId);

    boolean existsByStoreIdAndName(Long storeId, String name);
}
