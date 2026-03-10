package com.example.hungrypangproject.domain.store.controller;

import com.example.hungrypangproject.common.dto.ApiResponse;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.store.dto.request.StoreCreateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreStatusUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.response.StoreResponse;
import com.example.hungrypangproject.domain.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 식당 등록
    @PostMapping
    public ApiResponse<StoreResponse> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        // 로그인한 사용자 정보 가져오기
        StoreResponse response = storeService.createStore(request, userDetails.getMember());

        // 생성 성공 응답 반환 (201)
        return ApiResponse.created(response);
    }

    // 식당 목록 조회 (검색 기능)
    @GetMapping
    public ApiResponse<List<StoreResponse>> getStores(
            @RequestParam(required = false) String keyword
    ) {
        // keyword가 있으면 검색, 없으면 전체 조회
        return ApiResponse.ok(storeService.getStores(keyword));
    }

    // 식당 단건 조회
    @GetMapping("/{storeId}")
    public ApiResponse<StoreResponse> getStore(@PathVariable Long storeId) {
        // storeId로 식당 조회
        return ApiResponse.ok(storeService.getStore(storeId));
    }

    // 식당 수정
    @PatchMapping("/{storeId}")
    public ApiResponse<Void> updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        // 로그인한 판매자가 본인 식당 정보 수정
        storeService.updateStore(storeId, request, userDetails.getMember());

        return ApiResponse.ok();
    }

    // 식당 영업 상태 변경
    @PatchMapping("/{storeId}/status")
    public ApiResponse<Void> updateStoreStatus(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreStatusUpdateRequest request,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        // OPEN / CLOSED 등 영업 상태 변경
        storeService.updateStoreStatus(storeId, request, userDetails.getMember());

        return ApiResponse.ok();
    }

    // 식당 삭제
    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> deleteStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        // 로그인한 판매자가 본인 식당 삭제
        storeService.deleteStore(storeId, userDetails.getMember());

        // 삭제 성공 응답 (204)
        return ApiResponse.noContent();
    }
}
