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

    /**
     * 식당별 메뉴 등록
     * - 해당 식당이 존재하는지 확인
     * - 같은 식당에 동일한 메뉴 이름이 있는지 중복 검사
     * - 메뉴 생성 후 저장
     */
    public MenuResponse createMenu(Long storeId, MenuCreateRequest request) {

        // 식당 조회 (없으면 예외 발생)
        Store store = getStoreEntity(storeId);

        // 같은 식당에 동일한 메뉴 이름이 존재하는지 검사
        if (menuRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new MenuException(ErrorCode.MENU_DUPLICATE_NAME);
        }

        // 메뉴 엔티티 생성
        Menu menu = Menu.create(
                store,
                request.getName(),
                request.getPrice(),
                request.getStock(),
                MenuStatus.PREPARING // 메뉴 기본 상태
        );

        // 메뉴 저장 후 응답 반환
        Menu savedMenu = menuRepository.save(menu);
        return MenuResponse.from(savedMenu);
    }

    /**
     * 식당별 메뉴 목록 조회
     * - 해당 식당이 존재하는지 확인
     * - 해당 식당의 메뉴 목록을 최신순으로 조회
     */
    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusByStore(Long storeId) {

        // 식당 존재 여부 검증
        validateStoreExists(storeId);

        // 메뉴 목록 조회 후 DTO 변환
        return menuRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(MenuResponse::from)
                .toList();
    }

    /**
     * 메뉴 상세 조회
     * - menuId로 메뉴 조회
     */
    @Transactional(readOnly = true)
    public MenuResponse getMenu(Long menuId) {

        // 메뉴 조회 (없으면 예외 발생)
        Menu menu = getMenuEntity(menuId);

        return MenuResponse.from(menu);
    }

    /**
     * 메뉴 수정
     * - 메뉴 이름, 가격, 재고 수정
     */
    public MenuResponse updateMenu(Long menuId, MenuUpdateRequest request) {

        // 메뉴 조회
        Menu menu = getMenuEntity(menuId);

        // 메뉴 정보 수정
        menu.update(
                request.getName(),
                request.getPrice(),
                request.getStock()
        );

        return MenuResponse.from(menu);
    }

    /**
     * 메뉴 상태 변경
     * - 메뉴 판매 상태 변경 (예: SALE, SOLDOUT 등)
     */
    public MenuResponse updateMenuStatus(Long menuId, MenuStatusUpdateRequest request) {

        // 메뉴 조회
        Menu menu = getMenuEntity(menuId);

        // 상태 변경
        menu.updateStatus(request.getStatus());

        return MenuResponse.from(menu);
    }

    /**
     * 메뉴 삭제
     * - menuId로 메뉴 조회 후 삭제
     */
    public void deleteMenu(Long menuId) {

        // 메뉴 조회
        Menu menu = getMenuEntity(menuId);

        // 메뉴 삭제
        menuRepository.delete(menu);
    }

    /**
     * 메뉴 엔티티 조회
     * - 존재하지 않으면 MENU_NOT_FOUND 예외 발생
     */
    private Menu getMenuEntity(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(ErrorCode.MENU_NOT_FOUND));
    }

    /**
     * 식당 엔티티 조회
     * - 존재하지 않으면 STORE_NOT_FOUND 예외 발생
     */
    private Store getStoreEntity(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    }

    /**
     * 식당 존재 여부 검증
     * - 존재하지 않으면 STORE_NOT_FOUND 예외 발생
     */
    private void validateStoreExists(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }
    }
}
