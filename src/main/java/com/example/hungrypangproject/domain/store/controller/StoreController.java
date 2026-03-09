package com.example.hungrypangproject.domain.store.controller;

import com.example.hungrypangproject.common.dto.ApiResponse;
import com.example.hungrypangproject.domain.store.dto.request.StoreCreateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreStatusUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.response.StoreResponse;
import com.example.hungrypangproject.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 식당 등록
     * - 새로운 식당을 생성
     * - 요청 Body의 식당 정보(StoreCreateRequest)를 받아 저장
     * - 생성된 식당 정보를 응답으로 반환
     */
    @PostMapping
    public ApiResponse<StoreResponse> createStore(@Valid @RequestBody StoreCreateRequest request) {

        // 식당 생성 서비스 호출
        StoreResponse response = storeService.createStore(request);

        return ApiResponse.created(response);
    }

    /**
     * 식당 목록 조회
     * - keyword가 없으면 전체 식당 조회
     * - keyword가 있으면 식당 이름 검색
     */
    @GetMapping
    public ApiResponse<List<StoreResponse>> getStores(@RequestParam(required = false) String keyword) {

        // 서비스에서 식당 목록 조회
        return ApiResponse.ok(storeService.getStores(keyword));
    }

    /**
     * 식당 단건 조회
     * - storeId로 특정 식당 상세 정보 조회
     */
    @GetMapping("/{storeId}")
    public ApiResponse<StoreResponse> getStore(@PathVariable Long storeId) {

        // 식당 상세 조회
        return ApiResponse.ok(storeService.getStore(storeId));
    }

    /**
     * 식당 정보 수정
     * - 식당 이름, 배달비, 최소 주문 금액 수정
     */
    @PatchMapping("/{storeId}")
    public ApiResponse<Void> updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequest request
    ) {

        // 식당 정보 수정
        storeService.updateStore(storeId, request);

        return ApiResponse.ok();
    }

    /**
     * 식당 영업 상태 변경
     * - OPEN / CLOSED / PREPARING 상태 변경
     */
    @PatchMapping("/{storeId}/status")
    public ApiResponse<Void> updateStoreStatus(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreStatusUpdateRequest request
    ) {

        // 식당 상태 변경
        storeService.updateStoreStatus(storeId, request);

        return ApiResponse.ok();
    }

    /**
     * 식당 삭제
     * - storeId로 식당 삭제
     */
    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(@PathVariable Long storeId) {

        // 식당 삭제
        storeService.deleteStore(storeId);

        return ApiResponse.noContent();
    }
}
