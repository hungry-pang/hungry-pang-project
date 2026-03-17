package com.example.hungrypangproject.domain.menu.repository;

import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByStoreIdOrderByCreatedAtDesc(Long storeId);

    boolean existsByStoreIdAndName(Long storeId, String name);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Menu m WHERE m.id IN :ids AND m.store.id = :storeId")
    List<Menu> findAllByIdInAndStoreId(
            @Param("ids") List<Long> ids,
            @Param("storeId") Long storeId
    );
}
