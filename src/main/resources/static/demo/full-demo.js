// 전역 상태 관리
const state = {
    accessToken: '',
    refreshToken: '',
    currentMemberId: null,
    currentEmail: '',
    currentRole: '',
    isPaymentFlowRunning: false
};
// 유틸리티 함수
function el(id) {
    return document.getElementById(id);
}
function getBaseUrl() {
    return el('api-base-url').value.trim() || 'http://localhost:8080';
}
function log(title, data) {
    const now = new Date().toLocaleTimeString('ko-KR');
    const logContent = el('result-content');
    const entry = `[${now}] ${title}\n${JSON.stringify(data, null, 2)}\n\n`;
    logContent.textContent = entry + logContent.textContent;

    // 로그가 너무 길면 스크롤
    const resultBox = el('result-log');
    if (resultBox) {
        resultBox.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }
}
function clearLog() {
    el('result-content').textContent = '';
    log('로그 초기화', { message: '로그가 지워졌습니다.' });
}
function showAlert(message, type = 'info') {
    alert(message);
}
// 인증 정보 업데이트
function updateAuthInfo() {
    const authInfo = el('authInfo');
    const authEmail = el('authEmail');
    const authRole = el('authRole');

    if (state.accessToken) {
        authEmail.textContent = state.currentEmail || '로그인됨';
        authRole.textContent = state.currentRole || 'USER';
    } else {
        authEmail.textContent = '미로그인';
        authRole.textContent = '-';
    }
}
// API 요청 함수
async function apiRequest(path, options = {}) {
    const url = `${getBaseUrl()}${path}`;
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    if (state.accessToken) {
        headers['Authorization'] = state.accessToken;
    }
    try {
        const response = await fetch(url, {
            ...options,
            headers
        });
        const contentType = response.headers.get('content-type');
        let data;

        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }
        if (!response.ok) {
            throw {
                status: response.status,
                message: data.message || data || '요청 실패',
                data
            };
        }
        return { response, data };
    } catch (error) {
        log('❌ API 오류', error);
        throw error;
    }
}
// 섹션 표시
function showSection(sectionId, clickedElement) {
    // 모든 섹션 숨기기
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // 모든 메뉴 버튼 비활성화
    document.querySelectorAll('.menu-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 선택된 섹션 표시
    const section = el(sectionId);
    if (section) {
        section.classList.add('active');
    }

    // 선택된 메뉴 버튼 활성화
    const targetButton = clickedElement || (typeof event !== 'undefined' ? event.target : null);
    if (targetButton && targetButton.classList) {
        targetButton.classList.add('active');
    }
}
// 탭 표시
function showTab(sectionId, tabId, clickedElement) {
    const section = el(sectionId);
    if (!section) return;

    // 해당 섹션의 모든 탭 컨텐츠 숨기기
    section.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });

    // 해당 섹션의 모든 탭 버튼 비활성화
    section.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 선택된 탭 컨텐츠 표시
    const tabContent = el(`${sectionId}-${tabId}`);
    if (tabContent) {
        tabContent.classList.add('active');
    }

    // 선택된 탭 버튼 활성화
    const targetButton = clickedElement || (typeof event !== 'undefined' ? event.target : null);
    if (targetButton && targetButton.classList) {
        targetButton.classList.add('active');
    }
}

function updatePaymentFlowStatus(message, type = 'info') {
    const statusEl = el('payment-flow-status');
    if (!statusEl) return;
    statusEl.textContent = message;
    statusEl.className = `payment-flow-status status-${type}`;
}

function openPaymentSection() {
    showSection('payment');
    const paymentMenu = document.querySelector("button[onclick=\"showSection('payment')\"]");
    if (paymentMenu) {
        paymentMenu.classList.add('active');
    }
}
// ========================================
// 회원 인증 API
// ========================================
async function signup() {
    try {
        const payload = {
            email: el('signup-email').value.trim(),
            password: el('signup-password').value,
            nickname: el('signup-nickname').value.trim(),
            address: el('signup-address').value.trim(),
            phoneNo: el('signup-phone').value.trim(),
            role: el('signup-role').value,
            totalPoint: 0
        };
        const { data } = await apiRequest('/api/signup', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 회원가입 성공', data);
        showAlert('회원가입이 완료되었습니다!', 'success');
    } catch (error) {
        log('❌ 회원가입 실패', error);
        showAlert('회원가입 실패: ' + error.message, 'error');
    }
}
async function login() {
    try {
        const payload = {
            email: el('login-email').value.trim(),
            password: el('login-password').value
        };
        const { response, data } = await apiRequest('/api/login', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        state.accessToken = response.headers.get('Authorization') || '';
        state.refreshToken = response.headers.get('Refresh-Token') || '';
        state.currentEmail = payload.email;
        state.currentRole = data.data?.role || 'USER';
        state.currentMemberId = data.data?.memberId;
        updateAuthInfo();
        log('✅ 로그인 성공', {
            email: state.currentEmail,
            role: state.currentRole,
            hasToken: !!state.accessToken
        });
        showAlert('로그인 성공!', 'success');
    } catch (error) {
        log('❌ 로그인 실패', error);
        showAlert('로그인 실패: ' + error.message, 'error');
    }
}
async function logout() {
    try {
        if (state.accessToken) {
            await apiRequest('/api/logout', { method: 'POST' });
        }
    } catch (error) {
        log('⚠️ 로그아웃 요청 실패 (토큰 초기화는 진행)', error);
    } finally {
        state.accessToken = '';
        state.refreshToken = '';
        state.currentEmail = '';
        state.currentRole = '';
        state.currentMemberId = null;
        updateAuthInfo();
        log('✅ 로그아웃 완료', { message: '로그아웃되었습니다.' });
        showAlert('로그아웃되었습니다.', 'info');
    }
}
// ========================================
// 회원 정보 API
// ========================================
async function getMemberInfo() {
    try {
        const memberId = el('member-id').value.trim();
        if (!memberId) {
            showAlert('회원 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/v1/members/${memberId}`, {
            method: 'GET'
        });
        log('✅ 회원 정보 조회 성공', data);
    } catch (error) {
        log('❌ 회원 정보 조회 실패', error);
        showAlert('회원 정보 조회 실패: ' + error.message, 'error');
    }
}
async function updateMemberInfo() {
    try {
        const memberId = el('update-member-id').value.trim();
        const payload = {
            nickname: el('update-nickname').value.trim(),
            address: el('update-address').value.trim(),
            phoneNo: el('update-phone').value.trim()
        };
        if (!memberId) {
            showAlert('회원 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/members/${memberId}`, {
            method: 'PATCH',
            body: JSON.stringify(payload)
        });
        log('✅ 회원 정보 수정 성공', data);
        showAlert('회원 정보가 수정되었습니다.', 'success');
    } catch (error) {
        log('❌ 회원 정보 수정 실패', error);
        showAlert('회원 정보 수정 실패: ' + error.message, 'error');
    }
}
// ========================================
// 식당 관리 API
// ========================================
async function createStore() {
    try {
        const payload = {
            storeName: el('store-name').value.trim(),
            deliveryFee: Number(el('store-delivery-fee').value),
            minimumOrder: Number(el('store-minimum-order').value)
        };
        const { data } = await apiRequest('/api/stores', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 식당 등록 성공', data);
        showAlert('식당이 등록되었습니다!', 'success');
    } catch (error) {
        log('❌ 식당 등록 실패', error);
        showAlert('식당 등록 실패: ' + error.message, 'error');
    }
}
async function searchStores() {
    try {
        const keyword = el('store-search-keyword').value.trim();
        const url = keyword ? `/api/stores?keyword=${encodeURIComponent(keyword)}` : '/api/stores';
        const { data } = await apiRequest(url, { method: 'GET' });
        log('✅ 식당 검색 성공', data);
        displayStoreList(data.data || data);
    } catch (error) {
        log('❌ 식당 검색 실패', error);
        showAlert('식당 검색 실패: ' + error.message, 'error');
    }
}
async function getAllStores() {
    el('store-search-keyword').value = '';
    await searchStores();
}
function displayStoreList(stores) {
    const container = el('store-list-result');
    if (!container) return;
    if (!stores || stores.length === 0) {
        container.innerHTML = '<p class="hint">검색 결과가 없습니다.</p>';
        return;
    }
    const storeItems = Array.isArray(stores.content) ? stores.content : stores;

    container.innerHTML = storeItems.map(store => `
        <div class="list-item">
            <div>
                <strong>${store.storeName || store.name}</strong>
                <span class="badge badge-${store.status === 'OPEN' ? 'success' : 'secondary'}">
                    ${store.status === 'OPEN' ? '영업중' : '영업종료'}
                </span>
                <p style="margin: 5px 0; color: #6c757d; font-size: 13px;">
                    ID: ${store.id} | 배달비: ${store.deliveryFee}원 | 최소주문: ${store.minimumOrder}원
                </p>
            </div>
        </div>
    `).join('');
}
async function updateStore() {
    try {
        const storeId = el('update-store-id').value.trim();
        const payload = {
            storeName: el('update-store-name').value.trim(),
            deliveryFee: Number(el('update-delivery-fee').value),
            minimumOrder: Number(el('update-minimum-order').value)
        };
        if (!storeId) {
            showAlert('식당 ID를 입력하세요.', 'warning');
            return;
        }
        await apiRequest(`/api/stores/${storeId}`, {
            method: 'PATCH',
            body: JSON.stringify(payload)
        });
        log('✅ 식당 정보 수정 성공', { storeId, ...payload });
        showAlert('식당 정보가 수정되었습니다.', 'success');
    } catch (error) {
        log('❌ 식당 정보 수정 실패', error);
        showAlert('식당 정보 수정 실패: ' + error.message, 'error');
    }
}
async function updateStoreStatus() {
    try {
        const storeId = el('status-store-id').value.trim();
        const payload = {
            status: el('store-status').value
        };
        if (!storeId) {
            showAlert('식당 ID를 입력하세요.', 'warning');
            return;
        }
        await apiRequest(`/api/stores/${storeId}/status`, {
            method: 'PATCH',
            body: JSON.stringify(payload)
        });
        log('✅ 식당 상태 변경 성공', { storeId, ...payload });
        showAlert('식당 상태가 변경되었습니다.', 'success');
    } catch (error) {
        log('❌ 식당 상태 변경 실패', error);
        showAlert('식당 상태 변경 실패: ' + error.message, 'error');
    }
}
async function deleteStore() {
    try {
        const storeId = el('delete-store-id').value.trim();
        if (!storeId) {
            showAlert('식당 ID를 입력하세요.', 'warning');
            return;
        }
        if (!confirm('정말 이 식당을 삭제하시겠습니까?')) {
            return;
        }
        await apiRequest(`/api/stores/${storeId}`, { method: 'DELETE' });
        log('✅ 식당 삭제 성공', { storeId });
        showAlert('식당이 삭제되었습니다.', 'success');
    } catch (error) {
        log('❌ 식당 삭제 실패', error);
        showAlert('식당 삭제 실패: ' + error.message, 'error');
    }
}
// ========================================
// 메뉴 관리 API
// ========================================
async function createMenu() {
    try {
        const storeId = el('menu-store-id').value.trim();
        const payload = {
            name: el('menu-name').value.trim(),
            price: Number(el('menu-price').value),
            stock: Number(el('menu-stock').value)
        };
        if (!storeId) {
            showAlert('식당 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/stores/${storeId}/menus`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 메뉴 등록 성공', data);
        showAlert('메뉴가 등록되었습니다!', 'success');
    } catch (error) {
        log('❌ 메뉴 등록 실패', error);
        showAlert('메뉴 등록 실패: ' + error.message, 'error');
    }
}
async function getMenusByStore() {
    try {
        const storeId = el('menu-list-store-id').value.trim();
        if (!storeId) {
            showAlert('식당 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/stores/${storeId}/menus`, {
            method: 'GET'
        });
        log('✅ 메뉴 조회 성공', data);
        displayMenuList(data.data || data);
    } catch (error) {
        log('❌ 메뉴 조회 실패', error);
        showAlert('메뉴 조회 실패: ' + error.message, 'error');
    }
}
function displayMenuList(menus) {
    const container = el('menu-list-result');
    if (!container) return;
    if (!menus || menus.length === 0) {
        container.innerHTML = '<p class="hint">등록된 메뉴가 없습니다.</p>';
        return;
    }
    container.innerHTML = menus.map(menu => `
        <div class="list-item">
            <div>
                <strong>${menu.name}</strong>
                <span class="badge badge-primary">${menu.price}원</span>
                <span class="badge badge-secondary">재고: ${menu.stock}</span>
                <p style="margin: 5px 0; color: #6c757d; font-size: 13px;">
                    메뉴 ID: ${menu.menuId || menu.id} | 식당 ID: ${menu.storeId}
                </p>
            </div>
        </div>
    `).join('');
}
async function updateMenu() {
    try {
        const menuId = el('update-menu-id').value.trim();
        const payload = {
            name: el('update-menu-name').value.trim(),
            price: Number(el('update-menu-price').value),
            stock: Number(el('update-menu-stock').value)
        };
        if (!menuId) {
            showAlert('메뉴 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/menus/${menuId}`, {
            method: 'PATCH',
            body: JSON.stringify(payload)
        });
        log('✅ 메뉴 수정 성공', data);
        showAlert('메뉴가 수정되었습니다.', 'success');
    } catch (error) {
        log('❌ 메뉴 수정 실패', error);
        showAlert('메뉴 수정 실패: ' + error.message, 'error');
    }
}
async function deleteMenu() {
    try {
        const menuId = el('delete-menu-id').value.trim();
        if (!menuId) {
            showAlert('메뉴 ID를 입력하세요.', 'warning');
            return;
        }
        if (!confirm('정말 이 메뉴를 삭제하시겠습니까?')) {
            return;
        }
        await apiRequest(`/api/menus/${menuId}`, { method: 'DELETE' });
        log('✅ 메뉴 삭제 성공', { menuId });
        showAlert('메뉴가 삭제되었습니다.', 'success');
    } catch (error) {
        log('❌ 메뉴 삭제 실패', error);
        showAlert('메뉴 삭제 실패: ' + error.message, 'error');
    }
}
// ========================================
// 주문 관리 API
// ========================================
function addOrderItem() {
    const container = el('order-items-container');
    const newItem = document.createElement('div');
    newItem.className = 'order-item-input';
    newItem.innerHTML = `
        <input type="number" class="order-menu-id" placeholder="메뉴 ID" style="width: 45%;">
        <input type="number" class="order-quantity" placeholder="수량" style="width: 45%; margin-left: 2%;">
    `;
    container.appendChild(newItem);
}
async function createOrder() {
    try {
        const storeId = el('order-store-id').value.trim();
        const address = el('order-address').value.trim();
        const usedPoint = Number(el('order-use-point').value || 0);
        if (!storeId || !address) {
            showAlert('식당 ID와 배달 주소를 입력하세요.', 'warning');
            return;
        }
        // 주문 항목 수집
        const menuIds = document.querySelectorAll('.order-menu-id');
        const quantities = document.querySelectorAll('.order-quantity');
        const items = [];
        for (let i = 0; i < menuIds.length; i++) {
            const menuId = menuIds[i].value.trim();
            const quantity = quantities[i].value.trim();

            if (menuId && quantity) {
                items.push({
                    menuId: Number(menuId),
                    stock: Number(quantity)
                });
            }
        }
        if (items.length === 0) {
            showAlert('최소 1개 이상의 메뉴를 추가하세요.', 'warning');
            return;
        }
        const payload = {
            storeId: Number(storeId),
            items: items,
            deliveryAddress: address,
            usedPoint: usedPoint
        };
        const { data } = await apiRequest('/api/orders', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 주문 생성 성공', data);
        showAlert('주문이 생성되었습니다!', 'success');

        const createdOrder = data.data || data;
        const autoPayEnabled = el('order-auto-pay')?.checked === true;
        if (autoPayEnabled) {
            await startPaymentFlowFromOrder(createdOrder, usedPoint);
        }
    } catch (error) {
        log('❌ 주문 생성 실패', error);
        showAlert('주문 생성 실패: ' + error.message, 'error');
    }
}

async function startPaymentFlowFromOrder(orderData, usedPoint) {
    if (state.isPaymentFlowRunning) {
        log('⚠️ 자동 결제 중복 요청 무시', { orderId: orderData?.id });
        return;
    }

    try {
        state.isPaymentFlowRunning = true;
        openPaymentSection();
        updatePaymentFlowStatus('주문 생성 완료. 결제 준비 중...', 'pending');

        if (!orderData?.id || orderData?.totalPrice == null) {
            log('⚠️ 자동 결제 시작 실패', { reason: '주문 응답에서 id/totalPrice를 찾을 수 없습니다.', orderData });
            updatePaymentFlowStatus('자동 결제 시작 실패: 주문 정보가 부족합니다.', 'error');
            return;
        }

        // 주문 생성 응답으로 결제 입력값을 자동 채웁니다.
        el('payment-order-id').value = orderData.id;
        el('payment-amount').value = Number(orderData.totalPrice);
        el('payment-points').value = Number(usedPoint || 0);
        el('payment-buyer-name').value = el('payment-buyer-name').value || '테스트유저';
        el('payment-buyer-tel').value = el('payment-buyer-tel').value || '010-0000-0000';
        el('payment-buyer-email').value = el('payment-buyer-email').value || state.currentEmail || '';

        const preparedPayment = await preparePayment({ silentAlert: true });
        updatePaymentFlowStatus('결제 준비 완료. PortOne 결제창 호출 중...', 'pending');
        await requestPortOnePaymentAndVerify(preparedPayment);
        updatePaymentFlowStatus('결제 검증까지 완료되었습니다.', 'success');
    } catch (error) {
        log('❌ 주문 후 자동 결제 실패', error);
        updatePaymentFlowStatus('자동 결제 실패. 결제 탭에서 수동으로 진행하세요.', 'error');
        showAlert('주문은 생성되었지만 자동 결제 진행에 실패했습니다. 결제 탭에서 수동으로 진행해주세요.', 'warning');
    } finally {
        state.isPaymentFlowRunning = false;
    }
}
async function getMyOrders() {
    try {
        const { data } = await apiRequest('/api/orders', { method: 'GET' });
        log('✅ 주문 내역 조회 성공', data);
        displayOrderList(data.data || data);
    } catch (error) {
        log('❌ 주문 내역 조회 실패', error);
        showAlert('주문 내역 조회 실패: ' + error.message, 'error');
    }
}
function displayOrderList(orders) {
    const container = el('order-list-result');
    if (!container) return;
    if (!orders || orders.length === 0) {
        container.innerHTML = '<p class="hint">주문 내역이 없습니다.</p>';
        return;
    }
    container.innerHTML = orders.map(order => `
        <div class="list-item">
            <div>
                <strong>주문 #${order.orderId || order.id}</strong>
                <span class="badge badge-info">${order.orderStatus}</span>
                <p style="margin: 5px 0; color: #6c757d; font-size: 13px;">
                    식당: ${order.storeName} | 금액: ${order.totalPrice}원 | 주문시간: ${order.orderAt || ''}
                </p>
            </div>
        </div>
    `).join('');
}
async function getOrderDetail() {
    try {
        const orderId = el('order-detail-id').value.trim();
        if (!orderId) {
            showAlert('주문 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/orders/${orderId}`, {
            method: 'GET'
        });
        log('✅ 주문 상세 조회 성공', data);
    } catch (error) {
        log('❌ 주문 상세 조회 실패', error);
        showAlert('주문 상세 조회 실패: ' + error.message, 'error');
    }
}
async function updateOrderStatus() {
    try {
        const orderId = el('order-status-id').value.trim();
        const payload = {
            orderStatus: el('order-status').value
        };
        if (!orderId) {
            showAlert('주문 ID를 입력하세요.', 'warning');
            return;
        }
        await apiRequest(`/api/orders/${orderId}/status`, {
            method: 'PATCH',
            body: JSON.stringify(payload)
        });
        log('✅ 주문 상태 변경 성공', { orderId, ...payload });
        showAlert('주문 상태가 변경되었습니다.', 'success');
    } catch (error) {
        log('❌ 주문 상태 변경 실패', error);
        showAlert('주문 상태 변경 실패: ' + error.message, 'error');
    }
}
async function cancelOrder() {
    try {
        const orderId = el('cancel-order-id').value.trim();
        if (!orderId) {
            showAlert('주문 ID를 입력하세요.', 'warning');
            return;
        }
        if (!confirm('정말 이 주문을 취소하시겠습니까?')) {
            return;
        }
        await apiRequest(`/api/orders/${orderId}/cancel`, {
            method: 'PATCH'
        });
        log('✅ 주문 취소 성공', { orderId });
        showAlert('주문이 취소되었습니다.', 'success');
    } catch (error) {
        log('❌ 주문 취소 실패', error);
        showAlert('주문 취소 실패: ' + error.message, 'error');
    }
}
// ========================================
// 결제 처리 API
// ========================================
function mapPayMethodToPortOne(method) {
    const mapping = {
        EASY_PAY: 'EASY_PAY',
        CARD: 'CARD',
        TRANSFER: 'TRANSFER',
        VIRTUAL_ACCOUNT: 'VIRTUAL_ACCOUNT',
        card: 'CARD',
        trans: 'TRANSFER',
        vbank: 'VIRTUAL_ACCOUNT'
    };
    return mapping[method] || 'EASY_PAY';
}

async function preparePayment(options = {}) {
    const silentAlert = options.silentAlert === true;
    try {
        const payload = {
            orderId: Number(el('payment-order-id').value),
            amount: Number(el('payment-amount').value),
            pointsToUse: Number(el('payment-points').value || 0),
            payMethod: el('payment-method').value,
            buyerName: el('payment-buyer-name').value.trim(),
            buyerTel: el('payment-buyer-tel').value.trim(),
            buyerEmail: el('payment-buyer-email').value.trim()
        };
        const { data } = await apiRequest('/api/payments/prepare', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 결제 준비 성공', data);
        if (!silentAlert) {
            showAlert('결제 준비가 완료되었습니다!', 'success');
        }

        // 자동으로 DB 결제 ID 채우기
        const preparedPayment = data.data || data;
        if (preparedPayment?.dbPaymentId) {
            el('db-payment-id').value = preparedPayment.dbPaymentId;
        }
        return preparedPayment;
    } catch (error) {
        log('❌ 결제 준비 실패', error);
        if (!silentAlert) {
            showAlert('결제 준비 실패: ' + error.message, 'error');
        }
        throw error;
    }
}

async function requestPortOnePaymentAndVerify(preparedPayment) {
    const storeId = el('portone-store-id').value.trim();
    const channelKey = el('portone-channel-key').value.trim();

    if (!window.PortOne || typeof PortOne.requestPayment !== 'function') {
        throw new Error('PortOne SDK가 로드되지 않았습니다. 페이지를 새로고침해주세요.');
    }
    if (!storeId || !channelKey) {
        throw new Error('설정 탭에 PortOne Store ID와 Channel Key를 입력해주세요.');
    }
    if (!preparedPayment?.dbPaymentId) {
        throw new Error('결제 준비 응답에서 dbPaymentId를 찾을 수 없습니다.');
    }

    const payMethod = mapPayMethodToPortOne(el('payment-method').value || 'EASY_PAY');
    const customerName = preparedPayment.buyerName || el('payment-buyer-name').value.trim() || '테스트유저';
    const customerPhone = preparedPayment.buyerTel || el('payment-buyer-tel').value.trim() || '01000000000';
    const customerEmail = preparedPayment.buyerEmail || el('payment-buyer-email').value.trim() || state.currentEmail || 'demo@example.com';

    const portOnePayload = {
        storeId,
        channelKey,
        paymentId: preparedPayment.dbPaymentId,
        orderName: preparedPayment.orderName || `주문 ${preparedPayment.orderId}`,
        totalAmount: Number(preparedPayment.finalAmount ?? preparedPayment.amount ?? el('payment-amount').value),
        currency: 'CURRENCY_KRW',
        payMethod,
        customer: {
            fullName: customerName,
            phoneNumber: customerPhone,
            email: customerEmail
        },
        redirectUrl: window.location.href
    };

    log('ℹ️ PortOne 결제 요청 payload', portOnePayload);

    const paymentResult = await PortOne.requestPayment(portOnePayload);
    log('ℹ️ PortOne 결제 응답 raw', paymentResult);

    if (paymentResult.code != null) {
        log('❌ PortOne 결제 실패/취소', paymentResult);
        updatePaymentFlowStatus('PortOne 결제 요청이 실패했습니다. 결과 로그의 payload/response를 확인하세요.', 'error');
        showAlert('결제가 취소되었거나 실패했습니다: ' + (paymentResult.message || '결제 실패'), 'error');
        return;
    }

    // v2에서는 txId를 검증용 PortOne 거래 ID로 우선 사용합니다.
    const paymentId = paymentResult.txId || paymentResult.paymentId;
    const txId = paymentResult.txId || null;
    el('payment-id').value = paymentId || '';
    el('db-payment-id').value = preparedPayment.dbPaymentId;

    log('✅ PortOne 결제 성공', paymentResult);
    await verifyPaymentByValue(paymentId, preparedPayment.dbPaymentId, txId, true);
    showAlert('주문 생성부터 결제 검증까지 완료되었습니다!', 'success');
}

async function verifyPaymentByValue(paymentId, dbPaymentId, txId = null, silentAlert = false) {
    try {
        const payload = {
            paymentId: (paymentId || '').trim(),
            dbPaymentId: (dbPaymentId || '').trim(),
            txId
        };
        if (!payload.paymentId || !payload.dbPaymentId) {
            if (!silentAlert) {
                showAlert('결제 ID와 DB 결제 ID를 모두 입력하세요.', 'warning');
            }
            return;
        }
        const { data } = await apiRequest('/api/payments/verify', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 결제 검증 성공', data);
        if (!silentAlert) {
            showAlert('결제가 검증되었습니다!', 'success');
        }
        return data;
    } catch (error) {
        log('❌ 결제 검증 실패', error);
        if (!silentAlert) {
            showAlert('결제 검증 실패: ' + error.message, 'error');
        }
        throw error;
    }
}

async function verifyPayment() {
    const paymentId = el('payment-id').value.trim();
    const dbPaymentId = el('db-payment-id').value.trim();
    await verifyPaymentByValue(paymentId, dbPaymentId, null, false);
}
async function getPaymentDetail() {
    try {
        const dbPaymentId = el('payment-detail-id').value.trim();
        if (!dbPaymentId) {
            showAlert('DB 결제 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/payments/${encodeURIComponent(dbPaymentId)}`, {
            method: 'GET'
        });
        log('✅ 결제 상세 조회 성공', data);
    } catch (error) {
        log('❌ 결제 상세 조회 실패', error);
        showAlert('결제 상세 조회 실패: ' + error.message, 'error');
    }
}
// ========================================
// 환불 처리 API
// ========================================
async function requestRefund() {
    try {
        const dbPaymentId = el('refund-payment-id').value.trim();
        const payload = {
            orderId: Number(el('refund-order-id').value),
            reason: el('refund-reason').value.trim()
        };
        if (!dbPaymentId || !payload.orderId || !payload.reason) {
            showAlert('모든 필드를 입력하세요.', 'warning');
            return;
        }
        if (!confirm('정말 환불을 요청하시겠습니까?')) {
            return;
        }
        const { data } = await apiRequest(`/api/refunds/${encodeURIComponent(dbPaymentId)}`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 환불 요청 성공', data);
        showAlert('환불이 요청되었습니다!', 'success');
    } catch (error) {
        log('❌ 환불 요청 실패', error);
        showAlert('환불 요청 실패: ' + error.message, 'error');
    }
}
async function getRefundDetail() {
    try {
        const refundId = el('refund-detail-id').value.trim();
        if (!refundId) {
            showAlert('환불 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/refunds/${refundId}`, {
            method: 'GET'
        });
        log('✅ 환불 상세 조회 성공', data);
    } catch (error) {
        log('❌ 환불 상세 조회 실패', error);
        showAlert('환불 상세 조회 실패: ' + error.message, 'error');
    }
}
// ========================================
// 리뷰 관리 API
// ========================================
async function createReview() {
    try {
        const orderId = el('review-order-id').value.trim();
        const payload = {
            rating: Number(el('review-rating').value),
            content: el('review-content').value.trim()
        };
        if (!orderId) {
            showAlert('주문 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/orders/${orderId}/reviews`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 리뷰 작성 성공', data);
        showAlert('리뷰가 작성되었습니다!', 'success');
    } catch (error) {
        log('❌ 리뷰 작성 실패', error);
        showAlert('리뷰 작성 실패: ' + error.message, 'error');
    }
}
async function getStoreReviews() {
    try {
        const storeId = el('review-store-id').value.trim();
        if (!storeId) {
            showAlert('식당 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/stores/${storeId}/reviews`, {
            method: 'GET'
        });
        log('✅ 리뷰 조회 성공', data);
        displayReviewList(data.data || data);
    } catch (error) {
        log('❌ 리뷰 조회 실패', error);
        showAlert('리뷰 조회 실패: ' + error.message, 'error');
    }
}
function displayReviewList(reviews) {
    const container = el('review-list-result');
    if (!container) return;
    if (!reviews || reviews.length === 0) {
        container.innerHTML = '<p class="hint">등록된 리뷰가 없습니다.</p>';
        return;
    }
    container.innerHTML = reviews.map(review => `
        <div class="list-item">
            <div>
                <strong>⭐ ${review.rating}/5</strong>
                <p style="margin: 8px 0; color: #212529;">${review.content}</p>
                <p style="margin: 0; color: #6c757d; font-size: 12px;">
                    리뷰 ID: ${review.reviewId || review.id} | 작성자: ${review.memberNickname || '익명'}
                </p>
            </div>
        </div>
    `).join('');
}
async function updateReview() {
    try {
        const reviewId = el('update-review-id').value.trim();
        const payload = {
            rating: Number(el('update-review-rating').value),
            content: el('update-review-content').value.trim()
        };
        if (!reviewId) {
            showAlert('리뷰 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/reviews/${reviewId}`, {
            method: 'PATCH',
            body: JSON.stringify(payload)
        });
        log('✅ 리뷰 수정 성공', data);
        showAlert('리뷰가 수정되었습니다.', 'success');
    } catch (error) {
        log('❌ 리뷰 수정 실패', error);
        showAlert('리뷰 수정 실패: ' + error.message, 'error');
    }
}
async function deleteReview() {
    try {
        const reviewId = el('delete-review-id').value.trim();
        if (!reviewId) {
            showAlert('리뷰 ID를 입력하세요.', 'warning');
            return;
        }
        if (!confirm('정말 이 리뷰를 삭제하시겠습니까?')) {
            return;
        }
        await apiRequest(`/api/reviews/${reviewId}`, { method: 'DELETE' });
        log('✅ 리뷰 삭제 성공', { reviewId });
        showAlert('리뷰가 삭제되었습니다.', 'success');
    } catch (error) {
        log('❌ 리뷰 삭제 실패', error);
        showAlert('리뷰 삭제 실패: ' + error.message, 'error');
    }
}
// ========================================
// 멤버십 API
// ========================================
async function getMembershipInfo() {
    try {
        const memberId = el('membership-member-id').value.trim();
        if (!memberId) {
            showAlert('회원 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/membership/${memberId}`, {
            method: 'GET'
        });
        log('✅ 멤버십 정보 조회 성공', data);
    } catch (error) {
        log('❌ 멤버십 정보 조회 실패', error);
        showAlert('멤버십 정보 조회 실패: ' + error.message, 'error');
    }
}
// ========================================
// 배달 시스템 API
// ========================================
async function createDelivery() {
    try {
        const payload = {
            orderId: Number(el('delivery-order-id').value)
        };
        if (!payload.orderId) {
            showAlert('주문 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest('/api/deliverys', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 배달 요청 성공', data);
        showAlert('배달 요청이 생성되었습니다!', 'success');
    } catch (error) {
        log('❌ 배달 요청 실패', error);
        showAlert('배달 요청 실패: ' + error.message, 'error');
    }
}
async function completeDelivery() {
    try {
        const deliveryId = el('delivery-id').value.trim();
        if (!deliveryId) {
            showAlert('배달 ID를 입력하세요.', 'warning');
            return;
        }
        await apiRequest(`/api/deliverys/${deliveryId}/complete`, {
            method: 'PATCH'
        });
        log('✅ 배달 완료 처리 성공', { deliveryId });
        showAlert('배달이 완료되었습니다!', 'success');
    } catch (error) {
        log('❌ 배달 완료 처리 실패', error);
        showAlert('배달 완료 처리 실패: ' + error.message, 'error');
    }
}
async function getDeliveryDetail() {
    try {
        const orderId = el('delivery-order-detail-id').value.trim();
        if (!orderId) {
            showAlert('주문 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/deliverys/orders/${orderId}`, {
            method: 'GET'
        });
        log('✅ 배달 상태 조회 성공', data);
    } catch (error) {
        log('❌ 배달 상태 조회 실패', error);
        showAlert('배달 상태 조회 실패: ' + error.message, 'error');
    }
}
// ========================================
// 쿠폰 관리 API
// ========================================
async function createCoupon() {
    try {
        const payload = {
            couponName: el('coupon-name').value.trim(),
            discountAmount: Number(el('coupon-discount').value),
            totalQuantity: Number(el('coupon-quantity').value)
        };
        const { data } = await apiRequest('/api/coupons', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        log('✅ 쿠폰 생성 성공', data);
        showAlert('쿠폰이 생성되었습니다!', 'success');
    } catch (error) {
        log('❌ 쿠폰 생성 실패', error);
        showAlert('쿠폰 생성 실패: ' + error.message, 'error');
    }
}
async function issueCoupon() {
    try {
        const couponId = el('issue-coupon-id').value.trim();
        if (!couponId) {
            showAlert('쿠폰 ID를 입력하세요.', 'warning');
            return;
        }
        const { data } = await apiRequest(`/api/coupons/${couponId}/issue`, {
            method: 'POST'
        });
        log('✅ 쿠폰 발급 성공', data);
        showAlert('쿠폰을 받았습니다!', 'success');
    } catch (error) {
        log('❌ 쿠폰 발급 실패', error);
        showAlert('쿠폰 발급 실패: ' + error.message, 'error');
    }
}
// ========================================
// 초기화
// ========================================
document.addEventListener('DOMContentLoaded', () => {
    log('🚀 HungryPang 시연 페이지 초기화', {
        message: '모든 기능을 테스트해보세요!',
        timestamp: new Date().toISOString()
    });

    updateAuthInfo();
});