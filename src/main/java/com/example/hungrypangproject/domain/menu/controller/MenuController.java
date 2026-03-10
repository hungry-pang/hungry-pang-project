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
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 메뉴 등록
    @PostMapping("/stores/{storeId}/menus")
    public ResponseEntity<MenuResponse> createMenu(
            @PathVariable Long storeId,
            @Valid @RequestBody MenuCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createMenu(storeId, request));
    }

    // 메뉴 목록 조회(식당별)
    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<List<MenuResponse>> getMenusByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.getMenusByStore(storeId));
    }

    // 메뉴 상세 조회
    @GetMapping("/menus/{menuId}")
    public ResponseEntity<MenuResponse> getMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(menuService.getMenu(menuId));
    }

    // 메뉴 수정
    @PatchMapping("/menus/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest request
    ) {
        return ResponseEntity.ok(menuService.updateMenu(menuId, request));
    }

    // 메뉴 상태 변경
    @PatchMapping("/menus/{menuId}/status")
    public ResponseEntity<MenuResponse> updateMenuStatus(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(menuService.updateMenuStatus(menuId, request));
    }

    // 메뉴 삭제
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return ResponseEntity.noContent().build();
    }
}
