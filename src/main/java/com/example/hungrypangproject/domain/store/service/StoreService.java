package com.example.hungrypangproject.domain.store.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.store.dto.request.StoreCreateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreStatusUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.response.StoreResponse;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.entity.StoreStatus;
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

    /**
     * 식당 등록
     * - 요청값으로 Store 엔티티 생성
     * - DB에 저장 후 응답 DTO 반환
     */
    public StoreResponse createStore(StoreCreateRequest request) {

        // 식당 엔티티 생성
        Store store = Store.create(
                request.getStoreName(),
                request.getDeliveryFee(),
                request.getMinimumOrder()
        );

        // 식당 저장
        Store savedStore = storeRepository.save(store);

        // DTO 변환 후 반환
        return StoreResponse.from(savedStore);
    }

    /**
     * 식당 목록 조회
     * - keyword가 없으면 전체 조회
     * - keyword가 있으면 이름 검색
     */
    @Transactional(readOnly = true)
    public List<StoreResponse> getStores(String keyword) {

        List<Store> stores;

        // 검색어가 없으면 전체 식당 조회
        if (keyword == null || keyword.isBlank()) {
            stores = storeRepository.findAll();
        }
        // 검색어가 있으면 식당 이름으로 검색
        else {
            stores = storeRepository.findByStoreNameContaining(keyword);
        }

        // Entity → DTO 변환
        return stores.stream()
                .map(StoreResponse::from)
                .toList();
    }

    /**
     * 식당 단건 조회
     * - storeId로 식당 조회
     */
    @Transactional(readOnly = true)
    public StoreResponse getStore(Long storeId) {

        // 식당 조회 (없으면 예외 발생)
        Store store = findStoreById(storeId);

        return StoreResponse.from(store);
    }

    /**
     * 식당 정보 수정
     * - 식당 이름
     * - 배달비
     * - 최소 주문 금액
     */
    public void updateStore(Long storeId, StoreUpdateRequest request) {

        // 식당 조회
        Store store = findStoreById(storeId);

        // 식당 정보 수정
        store.update(
                request.getStoreName(),
                request.getDeliveryFee(),
                request.getMinimumOrder()
        );
    }

    /**
     * 식당 영업 상태 변경
     * - OPEN / CLOSED / PREPARING 상태 변경
     */
    public void updateStoreStatus(Long storeId, StoreStatusUpdateRequest request) {

        // 식당 조회
        Store store = findStoreById(storeId);

        // 상태 변경
        store.updateStatus(request.getStatus());
    }

    /**
     * 식당 삭제
     * - storeId로 식당 조회 후 삭제
     */
    public void deleteStore(Long storeId) {

        // 식당 조회
        Store store = findStoreById(storeId);

        // 식당 삭제
        storeRepository.delete(store);
    }

    /**
     * 식당 조회 공통 메서드
     * - storeId로 조회
     * - 존재하지 않으면 STORE_NOT_FOUND 예외 발생
     */
    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    }
}
