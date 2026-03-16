package com.example.hungrypangproject.domain.store.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import com.example.hungrypangproject.domain.store.dto.request.StoreCreateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreStatusUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.response.StoreResponse;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.exception.StoreException;
import com.example.hungrypangproject.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;

    // 식당 등록
    @CacheEvict(value = "stores", allEntries = true)
    public StoreResponse createStore(StoreCreateRequest request, Member member) {

        // 판매자 권한 검증
        validateSeller(member);

        // 식당 생성
        Store store = Store.create(
                request.getStoreName(),
                request.getDeliveryFee(),
                request.getMinimumOrder(),
                member
        );

        // DB 저장
        Store savedStore = storeRepository.save(store);

        // 응답 DTO 변환
        return StoreResponse.from(savedStore);
    }

    // 식당 목록 조회 (검색 기능)
    @Cacheable(value = "stores", key = "#keyword + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<StoreResponse> getStores(String keyword, Pageable pageable) {
        Page<Store> stores;

        if (keyword == null || keyword.isBlank()) {
            stores = storeRepository.findAll(pageable);
        } else {
            stores = storeRepository.findByStoreNameContaining(keyword, pageable);
        }

        return stores.map(StoreResponse::from);
    }

    // 식당 단건 조회
    @Cacheable(value = "store", key = "#storeId")
    @Transactional(readOnly = true)
    public StoreResponse getStore(Long storeId) {
        Store store = findStoreById(storeId);
        return StoreResponse.from(store);
    }

    // 식당 정보 수정
    @Caching(evict = {@CacheEvict(value = "stores", allEntries = true), @CacheEvict(value = "store", key = "#storeId")})
    public void updateStore(Long storeId, StoreUpdateRequest request, Member member) {

        // 판매자 권한 검증
        validateSeller(member);

        // 식당 조회
        Store store = findStoreById(storeId);

        // 본인 식당인지 검증
        validateStoreOwner(store, member);

        // 식당 정보 수정
        store.update(
                request.getStoreName(),
                request.getDeliveryFee(),
                request.getMinimumOrder()
        );
    }

    // 식당 영업 상태 변경
    @Caching(evict = {@CacheEvict(value = "stores", allEntries = true), @CacheEvict(value = "store", key = "#storeId")})
    public void updateStoreStatus(Long storeId, StoreStatusUpdateRequest request, Member member) {

        // 판매자 권한 검증
        validateSeller(member);

        // 식당 조회
        Store store = findStoreById(storeId);

        // 본인 식당인지 검증
        validateStoreOwner(store, member);

        // 영업 상태 변경
        store.updateStatus(request.getStatus());
    }

    // 식당 삭제
    @Caching(evict = {@CacheEvict(value = "stores", allEntries = true), @CacheEvict(value = "store", key = "#storeId")})
    public void deleteStore(Long storeId, Member member) {

        // 판매자 권한 검증
        validateSeller(member);

        // 식당 조회
        Store store = findStoreById(storeId);

        // 본인 식당인지 검증
        validateStoreOwner(store, member);

        // 식당 삭제
        storeRepository.delete(store);
    }

    // 식당 조회 공통 메서드
    // 존재하지 않으면 예외 발생
    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    }

    // 판매자 권한 체크
    // SELLER 권한이 아니면 예외 발생
    private void validateSeller(Member member) {
        if (member.getRole() != MemberRoleEnum.ROLE_SELLER) {
            throw new StoreException(ErrorCode.STORE_ONLY_SELLER);
        }
    }

    // 본인 식당인지 검증
    // 식당 owner와 요청한 사용자가 다르면 예외 발생
    private void validateStoreOwner(Store store, Member member) {
        if (!store.isOwner(member.getMemberId())) {
            throw new StoreException(ErrorCode.STORE_FORBIDDEN);
        }
    }
}
