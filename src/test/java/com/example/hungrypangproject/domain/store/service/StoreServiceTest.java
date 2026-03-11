package com.example.hungrypangproject.domain.store.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import com.example.hungrypangproject.domain.store.dto.request.StoreCreateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreStatusUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.request.StoreUpdateRequest;
import com.example.hungrypangproject.domain.store.dto.response.StoreResponse;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.entity.StoreStatus;
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
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    private Member seller;
    private Member user;
    private Member anotherSeller;
    private Store store;

    @BeforeEach
    void setUp() {
        seller = mock(Member.class);
        user = mock(Member.class);
        anotherSeller = mock(Member.class);

        store = Store.create(
                "배고팡",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(15000),
                seller
        );
        ReflectionTestUtils.setField(store, "id", 1L);
    }

    @Test
    @DisplayName("식당 등록 성공 - 판매자는 식당을 등록할 수 있다")
    void createStore_Success() {

        when(seller.getRole()).thenReturn(MemberRoleEnum.ROLE_SELLER);

        StoreCreateRequest request = new StoreCreateRequest();
        ReflectionTestUtils.setField(request, "storeName", "배고팡");
        ReflectionTestUtils.setField(request, "deliveryFee", BigDecimal.valueOf(3000));
        ReflectionTestUtils.setField(request, "minimumOrder", BigDecimal.valueOf(1500));

        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store savedStore = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedStore, "id", 1L);
            return savedStore;
        });

        StoreResponse response = storeService.createStore(request, seller);

        assertNotNull(response);
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("식당 등록 실패 - 일반 회원은 식당을 등록할 수 없다")
    void createStore_Fail_NotSeller() {

        StoreCreateRequest request = new StoreCreateRequest();
        ReflectionTestUtils.setField(request, "storeName", "배고팡");
        ReflectionTestUtils.setField(request, "deliveryFee", BigDecimal.valueOf(3000));
        ReflectionTestUtils.setField(request, "minimumOrder", BigDecimal.valueOf(15000));

        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.createStore(request, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(ErrorCode.STORE_ONLY_SELLER.getMessage(), exception.getMessage());
        verify(storeRepository, never()).save(any(Store.class));
    }

    @Test
    @DisplayName("식당 목록 조회 성공 - 검색어가 있으면 이름으로 검색")
    void getStores_Success_WithKeyword() {

        when(storeRepository.findByStoreNameContaining("배고"))
                .thenReturn(List.of(store));

        List<StoreResponse> result = storeService.getStores("배고");

        assertEquals(1, result.size());
        verify(storeRepository, times(1)).findByStoreNameContaining("배고");
        verify(storeRepository, never()).findAll();
    }

    @Test
    @DisplayName("식당 단건 조회 성공")
    void getStore_Success() {

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        StoreResponse response = storeService.getStore(1L);

        assertNotNull(response);
        verify(storeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("식당 단건 조회 실패 - 존재하지 않는 식당")
    void getStore_Fail_NotFound() {

        when(storeRepository.findById(999L)).thenReturn(Optional.empty());


        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.getStore(999L));


        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals(ErrorCode.STORE_NOT_FOUND.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("식당 정보 수정 성공 - 본인 식당은 수정할 수 있다")
    void updateStore_Success() {

        when(seller.getRole()).thenReturn(MemberRoleEnum.ROLE_SELLER);

        StoreUpdateRequest request = new StoreUpdateRequest();
        ReflectionTestUtils.setField(request, "storeName", "수정된가게");
        ReflectionTestUtils.setField(request, "deliveryFee", BigDecimal.valueOf(4000));
        ReflectionTestUtils.setField(request, "minimumOrder", BigDecimal.valueOf(20000));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        storeService.updateStore(1L, request, seller);

        assertEquals("수정된가게", store.getStoreName());
        assertEquals(BigDecimal.valueOf(4000), store.getDeliveryFee());
        assertEquals(BigDecimal.valueOf(20000), store.getMinimumOrder());
    }

    @Test
    @DisplayName("식당 정보 수정 실패 - 일반 회원은 수정할 수 없다")
    void updateStore_Fail_NotSeller() {

        StoreUpdateRequest request = new StoreUpdateRequest();
        ReflectionTestUtils.setField(request, "storeName", "수정된가게");
        ReflectionTestUtils.setField(request, "deliveryFee", BigDecimal.valueOf(4000));
        ReflectionTestUtils.setField(request, "minimumOrder", BigDecimal.valueOf(20000));

        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.updateStore(1L, request, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(ErrorCode.STORE_ONLY_SELLER.getMessage(), exception.getMessage());
        verify(storeRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("식당 정보 수정 실패 - 다른 판매자의 식당은 수정할 수 없다")
    void updateStore_Fail_Forbidden() {

        when(anotherSeller.getRole()).thenReturn(MemberRoleEnum.ROLE_SELLER);
        when(anotherSeller.getMemberId()).thenReturn(3L);
        when(seller.getMemberId()).thenReturn(1L);

        StoreUpdateRequest request = new StoreUpdateRequest();
        ReflectionTestUtils.setField(request, "storeName", "수정된가게");
        ReflectionTestUtils.setField(request, "deliveryFee", BigDecimal.valueOf(4000));
        ReflectionTestUtils.setField(request, "minimumOrder", BigDecimal.valueOf(20000));

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.updateStore(1L, request, anotherSeller));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(ErrorCode.STORE_FORBIDDEN.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("식당 영업 상태 변경 성공 - 본인 식당의 상태를 변경할 수 있다")
    void updateStoreStatus_Success() {

        when(seller.getRole()).thenReturn(MemberRoleEnum.ROLE_SELLER);
        when(seller.getMemberId()).thenReturn(1L);

        StoreStatusUpdateRequest request = new StoreStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", StoreStatus.CLOSED);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        storeService.updateStoreStatus(1L, request, seller);

        assertEquals(StoreStatus.CLOSED, store.getStatus());
    }

    @Test
    @DisplayName("식당 영업 상태 변경 실패 - 일반 회원은 상태를 변경할 수 없다")
    void updateStoreStatus_Fail_NotSeller() {

        StoreStatusUpdateRequest request = new StoreStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", StoreStatus.CLOSED);

        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.updateStoreStatus(1L, request, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(ErrorCode.STORE_ONLY_SELLER.getMessage(), exception.getMessage());
        verify(storeRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("식당 영업 상태 변경 실패 - 다른 판매자의 식당은 변경할 수 없다")
    void updateStoreStatus_Fail_Forbidden() {

        when(anotherSeller.getRole()).thenReturn(MemberRoleEnum.ROLE_SELLER);
        when(anotherSeller.getMemberId()).thenReturn(3L);
        when(seller.getMemberId()).thenReturn(1L);

        StoreStatusUpdateRequest request = new StoreStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "status", StoreStatus.CLOSED);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.updateStoreStatus(1L, request, anotherSeller));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(ErrorCode.STORE_FORBIDDEN.getMessage(), exception.getMessage());
    }

    @Test
    @DisplayName("식당 삭제 성공 - 본인 식당은 삭제할 수 있다")
    void deleteStore_Success() {

        when(seller.getRole()).thenReturn(MemberRoleEnum.ROLE_SELLER);
        when(seller.getMemberId()).thenReturn(1L);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        storeService.deleteStore(1L, seller);

        verify(storeRepository, times(1)).delete(store);
    }

    @Test
    @DisplayName("식당 삭제 실패 - 일반 회원은 삭제할 수 없다")
    void deleteStore_Fail_NotSeller() {

        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.deleteStore(1L, user));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(ErrorCode.STORE_ONLY_SELLER.getMessage(), exception.getMessage());
        verify(storeRepository, never()).findById(anyLong());
        verify(storeRepository, never()).delete(any(Store.class));
    }

    @Test
    @DisplayName("식당 삭제 실패 - 다른 판매자의 식당은 삭제할 수 없다")
    void deleteStore_Fail_Forbidden() {

        when(anotherSeller.getRole()).thenReturn(MemberRoleEnum.ROLE_SELLER);
        when(anotherSeller.getMemberId()).thenReturn(3L);
        when(seller.getMemberId()).thenReturn(1L);

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        StoreException exception = assertThrows(StoreException.class,
                () -> storeService.deleteStore(1L, anotherSeller));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals(ErrorCode.STORE_FORBIDDEN.getMessage(), exception.getMessage());
        verify(storeRepository, never()).delete(any(Store.class));
    }
}
