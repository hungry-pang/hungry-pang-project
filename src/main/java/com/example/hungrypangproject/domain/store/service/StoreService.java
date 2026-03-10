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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

    private final StoreRepository storeRepository;

    // 식당 등록
    public StoreResponse createStore(StoreCreateRequest request, Member member) {
        validateSeller(member);

        Store store = Store.create(
                request.getStoreName(),
                request.getDeliveryFee(),
                request.getMinimumOrder(),
                member
        );

        Store savedStore = storeRepository.save(store);

        return StoreResponse.from(savedStore);
    }

    // 식당 목록 조회
    @Transactional(readOnly = true)
    public List<StoreResponse> getStores(String keyword) {
        List<Store> stores;

        if (keyword == null || keyword.isBlank()) {
            stores = storeRepository.findAll();
        } else {
            stores = storeRepository.findByStoreNameContaining(keyword);
        }

        return stores.stream()
                .map(StoreResponse::from)
                .toList();
    }

    // 식당 단건 조회
    @Transactional(readOnly = true)
    public StoreResponse getStore(Long storeId) {
        Store store = findStoreById(storeId);
        return StoreResponse.from(store);
    }

    // 식당 정보 수정
    public void updateStore(Long storeId, StoreUpdateRequest request, Member member) {
        validateSeller(member);

        Store store = findStoreById(storeId);
        validateStoreOwner(store, member);

        store.update(
                request.getStoreName(),
                request.getDeliveryFee(),
                request.getMinimumOrder()
        );
    }

    // 식당 영업 상태 변경
    public void updateStoreStatus(Long storeId, StoreStatusUpdateRequest request, Member member) {
        validateSeller(member);

        Store store = findStoreById(storeId);
        validateStoreOwner(store, member);

        store.updateStatus(request.getStatus());
    }

    // 식당 삭제
    public void deleteStore(Long storeId, Member member) {
        validateSeller(member);

        Store store = findStoreById(storeId);
        validateStoreOwner(store, member);

        storeRepository.delete(store);
    }

    // 식당 조회 공통 메서드 (없으면 예외 발생)
    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    }

    // 판매자 권한 체크
    private void validateSeller(Member member) {
        if (member.getRole() != MemberRoleEnum.ROLE_SELLER) {
            throw new StoreException(ErrorCode.STORE_ONLY_SELLER);
        }
    }

    // 본인 식당인지 체크
    private void validateStoreOwner(Store store, Member member) {
        if (!store.isOwner(member.getMemberId())) {
            throw new StoreException(ErrorCode.STORE_FORBIDDEN);
        }
    }
}
