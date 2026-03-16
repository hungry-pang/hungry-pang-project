package com.example.hungrypangproject.domain.store.repository;

import com.example.hungrypangproject.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Page<Store> findByStoreNameContaining(String keyword, Pageable pageable);
}
