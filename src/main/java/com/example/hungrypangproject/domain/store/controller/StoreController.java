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

    // 식당 등록
    @PostMapping
    public ApiResponse<StoreResponse> createStore(@Valid @RequestBody StoreCreateRequest request) {
        StoreResponse response = storeService.createStore(request);

        return ApiResponse.created(response);
    }

    // 식당 목록 조회
    @GetMapping
    public ApiResponse<List<StoreResponse>> getStores(@RequestParam(required = false) String keyword) {

        return ApiResponse.ok(storeService.getStores(keyword));
    }

    // 식당 단건 조회
    @GetMapping("/{storeId}")
    public ApiResponse<StoreResponse> getStore(@PathVariable Long storeId) {
        return ApiResponse.ok(storeService.getStore(storeId));
    }

    // 식당 수정
    @PatchMapping("/{storeId}")
    public ApiResponse<Void> updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequest request
    ) {
        storeService.updateStore(storeId, request);

        return ApiResponse.ok();
    }

    // 식당 영업 상태 변경
    @PatchMapping("/{storeId}/status")
    public ApiResponse<Void> updateStoreStatus(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreStatusUpdateRequest request
    ) {
        storeService.updateStoreStatus(storeId, request);

        return ApiResponse.ok();
    }

    // 식당 삭제
    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(@PathVariable Long storeId) {
        storeService.deleteStore(storeId);

        return ApiResponse.noContent();
    }
}
