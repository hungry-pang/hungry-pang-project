package com.example.hungrypangproject.domain.store.repository;

import com.example.hungrypangproject.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
