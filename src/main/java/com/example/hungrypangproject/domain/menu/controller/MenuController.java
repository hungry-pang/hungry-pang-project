package com.example.hungrypangproject.domain.menu.controller;

import com.example.hungrypangproject.domain.menu.dto.request.MenuCreateRequest;
import com.example.hungrypangproject.domain.menu.dto.request.MenuStatusUpdateRequest;
import com.example.hungrypangproject.domain.menu.dto.request.MenuUpdateRequest;
import com.example.hungrypangproject.domain.menu.dto.response.MenuResponse;
import com.example.hungrypangproject.domain.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 식당별 메뉴 등록
     * - 특정 식당에 새로운 메뉴를 추가
     * - 성공 시 201 CREATED 반환
     */
    @PostMapping("/stores/{storeId}/menus")
    public ResponseEntity<MenuResponse> createMenu(
            @PathVariable Long storeId,
            @Valid @RequestBody MenuCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createMenu(storeId, request));
    }

    /**
     * 식당별 메뉴 목록 조회
     * - 특정 식당의 모든 메뉴를 최신순으로 조회
     */
    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<List<MenuResponse>> getMenusByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.getMenusByStore(storeId));
    }

    /**
     * 메뉴 상세 조회
     * - menuId를 통해 특정 메뉴의 상세 정보를 조회
     */
    @GetMapping("/menus/{menuId}")
    public ResponseEntity<MenuResponse> getMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(menuService.getMenu(menuId));
    }

    /**
     * 메뉴 수정
     * - 메뉴 이름, 가격, 재고 등을 수정
     */
    @PatchMapping("/menus/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest request
    ) {
        return ResponseEntity.ok(menuService.updateMenu(menuId, request));
    }

    /**
     * 메뉴 상태 변경
     * - 메뉴 판매 상태 변경 (PREPARING, SALE, SOLDOUT)
     */
    @PatchMapping("/menus/{menuId}/status")
    public ResponseEntity<MenuResponse> updateMenuStatus(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(menuService.updateMenuStatus(menuId, request));
    }

    /**
     * 메뉴 삭제
     * - menuId로 메뉴 삭제
     * - 성공 시 204 NO CONTENT 반환
     */
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.noContent().build();
    }
}
