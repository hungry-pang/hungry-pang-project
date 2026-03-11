package com.example.hungrypangproject.domain.menu.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private MenuService menuService;

    private Store store;
    private Menu menu;

    @BeforeEach
    void setUp() {
        store = mock(Store.class);

        menu = Menu.create(
                store,
                "치킨",
                BigDecimal.valueOf(20000),
                50L,
                MenuStatus.PREPARING
        );

        ReflectionTestUtils.setField(menu, "id", 1L);
    }

    @Test
    @DisplayName("메뉴 등록 성공")
    void createMenu_Success() {

        // given
        MenuCreateRequest request = new MenuCreateRequest();
        ReflectionTestUtils.setField(request, "name", "피자");
        ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(25000));
        ReflectionTestUtils.setField(request, "stock", 35L);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(menuRepository.existsByStoreIdAndName(1L, "피자")).thenReturn(false);

        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> {
            Menu savedMenu = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedMenu, "id", 2L);
            return savedMenu;
        });

        // when
        MenuResponse response = menuService.createMenu(1L, request);

        // then
        assertNotNull(response);
        assertEquals("피자", response.getName());
        assertEquals(BigDecimal.valueOf(25000), response.getPrice());
        assertEquals(35L, response.getStock());
        assertEquals(MenuStatus.PREPARING, response.getStatus());

        verify(storeRepository, times(1)).findById(1L);
        verify(menuRepository, times(1)).existsByStoreIdAndName(1L, "피자");
        verify(menuRepository, times(1)).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 등록 실패 - 식당이 존재하지 않음")
    void createMenu_Fail_StoreNotFound() {
        // given
        MenuCreateRequest request = new MenuCreateRequest();
        ReflectionTestUtils.setField(request, "name", "피자");
        ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(25000));
        ReflectionTestUtils.setField(request, "stock", 35L);

        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        StoreException exception = assertThrows(StoreException.class,
                () -> menuService.createMenu(1L, request));

        // then
        assertEquals(ErrorCode.STORE_NOT_FOUND.getMessage(), exception.getMessage());
        verify(storeRepository, times(1)).findById(1L);
        verify(menuRepository, never()).existsByStoreIdAndName(anyLong(), anyString());
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 등록 실패 - 동일 식당 내 메뉴명 중복")
    void createMenu_Fail_DuplicateName() {
        // given
        MenuCreateRequest request = new MenuCreateRequest();
        ReflectionTestUtils.setField(request, "name", "치킨");
        ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(25000));
        ReflectionTestUtils.setField(request, "stock", 35L);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(menuRepository.existsByStoreIdAndName(1L, "치킨")).thenReturn(true);

        // when
        MenuException exception = assertThrows(MenuException.class,
                () -> menuService.createMenu(1L, request));

        // then
        assertEquals(ErrorCode.MENU_DUPLICATE_NAME.getMessage(), exception.getMessage());
        verify(storeRepository, times(1)).findById(1L);
        verify(menuRepository, times(1)).existsByStoreIdAndName(1L, "치킨");
        verify(menuRepository, never()).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 목록 조회 성공 - 식당별 메뉴 목록 조회")
    void getMenuByStore_Success() {
        // given
        when(storeRepository.existsById(1L)).thenReturn(true);
        when(menuRepository.findAllByStoreIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(menu));

        // when
        List<MenuResponse> result = menuService.getMenusByStore(1L);

        // then
        assertEquals(1, result.size());
        verify(storeRepository, times(1)).existsById(1L);
        verify(menuRepository, times(1)).findAllByStoreIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("메뉴 목록 조회 실패 - 존재하지 않는 식당")
    void getMenusByStore_Fail_StoreNotFound() {
        // given
        when(storeRepository.existsById(1L)).thenReturn(false);

        // when
        StoreException exception = assertThrows(StoreException.class,
                () -> menuService.getMenusByStore(1L));

        // then
        assertEquals(ErrorCode.STORE_NOT_FOUND.getMessage(), exception.getMessage());
        verify(storeRepository, times(1)).existsById(1L);
        verify(menuRepository, never()).findAllByStoreIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("메뉴 상세 조회 성공")
    void getMenu_Success() {
        // given
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        // when
        MenuResponse response = menuService.getMenu(1L);

        // then
        assertNotNull(response);
        verify(menuRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("메뉴 상세 조회 실패 - 존재하지 않는 메뉴")
    void getMenu_Fail_NotFound() {
        // given
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        MenuException exception = assertThrows(MenuException.class,
                () -> menuService.getMenu(999L));

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals(ErrorCode.MENU_NOT_FOUND.getMessage(), exception.getMessage());
        verify(menuRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("메뉴 수정 성공")
    void updateMenu_Success() {
        // given
        MenuUpdateRequest request = new MenuUpdateRequest();
        ReflectionTestUtils.setField(request, "name", "간장치킨");
        ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(25000));
        ReflectionTestUtils.setField(request, "stock", 50L);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        // when
        MenuResponse response = menuService.updateMenu(1L, request);

        // then
        assertEquals("간장치킨", response.getName());
        assertEquals(BigDecimal.valueOf(25000), response.getPrice());
        assertEquals(50L, response.getStock());
        verify(menuRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("메뉴 수정 실패 - 존재하지 않는 메뉴")
    void updateMenu_Fail_NotFound() {
        // given
        MenuUpdateRequest request = new MenuUpdateRequest();
        ReflectionTestUtils.setField(request, "name", "간장치킨");
        ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(25000));
        ReflectionTestUtils.setField(request, "stock", 50L);

        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        MenuException exception = assertThrows(MenuException.class,
                () -> menuService.updateMenu(999L, request));

        // then
        assertEquals(ErrorCode.MENU_NOT_FOUND.getMessage(), exception.getMessage());
        verify(menuRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("메뉴 상태 변경 성공")
    void updateMenuStatus_Success() {
        // given
        MenuStatusUpdateRequest request = new MenuStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", MenuStatus.SALE);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        // when
        MenuResponse response = menuService.updateMenuStatus(1L, request);

        // then
        assertEquals(MenuStatus.SALE, response.getStatus());
        verify(menuRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("메뉴 상태 변경 실패 - 존재하지 않는 메뉴")
    void updateMenuStatus_Fail_NotFound() {
        // given
        MenuStatusUpdateRequest request = new MenuStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", MenuStatus.SALE);

        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        MenuException exception = assertThrows(MenuException.class,
                () -> menuService.updateMenuStatus(999L, request));

        // then
        assertEquals(ErrorCode.MENU_NOT_FOUND.getMessage(), exception.getMessage());
        verify(menuRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("메뉴 삭제 성공")
    void deleteMenu_Success() {
        // given
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        // when
        menuService.deleteMenu(1L);

        // then
        verify(menuRepository, times(1)).findById(1L);
        verify(menuRepository, times(1)).delete(menu);
    }

    @Test
    @DisplayName("메뉴 삭제 실패 - 존재하지 않는 메뉴")
    void deleteMenu_Fail_NotFound() {
        // given
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        MenuException exception = assertThrows(MenuException.class,
                () -> menuService.deleteMenu(999L));

        // then
        assertEquals(ErrorCode.MENU_NOT_FOUND.getMessage(), exception.getMessage());
        verify(menuRepository, times(1)).findById(999L);
        verify(menuRepository, never()).delete(any(Menu.class));
    }
}
