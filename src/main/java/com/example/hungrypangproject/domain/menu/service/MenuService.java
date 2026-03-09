package com.example.hungrypangproject.domain.menu.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.domain.menu.dto.request.MenuCreateRequest;
import com.example.hungrypangproject.domain.menu.dto.request.MenuStatusUpdateRequest;
import com.example.hungrypangproject.domain.menu.dto.request.MenuUpdateRequest;
import com.example.hungrypangproject.domain.menu.dto.response.MenuResponse;
import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import com.example.hungrypangproject.domain.menu.exception.MenuException;
import com.example.hungrypangproject.domain.menu.repository.MenuRepository;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.exception.StoreException;
import com.example.hungrypangproject.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    // 메뉴 목록 조회(식당별)
    public List<MenuResponse> getMenusByStore(Long storeId) {
        validateStoreExists(storeId);

        return menuRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    // 메뉴 상세 조회
    @Transactional(readOnly = true)
    public MenuResponse getMenu(Long menuId) {
        Menu menu = getMenuEntity(menuId);
        return MenuResponse.from(menu);
    }

    // 메뉴 등록
    @Transactional(readOnly = true)
    public MenuResponse createMenu(Long storeId, MenuCreateRequest request) {
        Store store = getStoreEntity(storeId);

        if (menuRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new MenuException(ErrorCode.MENU_DUPLICATE_NAME);
        }

        Menu menu = Menu.create(
                store,
                request.getName(),
                request.getPrice(),
                request.getStock(),
                MenuStatus.PREPARING
        );

        Menu savedMenu = menuRepository.save(menu);
        return MenuResponse.from(savedMenu);
    }

    // 메뉴 수정
    public MenuResponse updateMenu(Long menuId, MenuUpdateRequest request) {
        Menu menu = getMenuEntity(menuId);

        menu.update(
                request.getName(),
                request.getPrice(),
                request.getStock()
        );

        return MenuResponse.from(menu);
    }

    // 메뉴 상태 변경
    public MenuResponse updateMenuStatus(Long menuId, MenuStatusUpdateRequest request) {
        Menu menu = getMenuEntity(menuId);
        menu.updateStatus(request.getStatus());
        return MenuResponse.from(menu);
    }

    // 메뉴 삭제
    public void deleteMenu(Long menuId) {
        Menu menu = getMenuEntity(menuId);
        menuRepository.delete(menu);
    }

    private Menu getMenuEntity(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(ErrorCode.MENU_NOT_FOUND));
    }

    private Store getStoreEntity(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateStoreExists(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }
    }
}
