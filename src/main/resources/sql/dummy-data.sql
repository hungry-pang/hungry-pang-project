-- =========================================================
-- MySQL 8.4.8
-- 배달 플랫폼 더미데이터 생성 스크립트
-- members / stores / menus / orders / reviews
-- 리뷰 5,000,000건 생성
-- DB: test
-- =========================================================

USE test;

-- ---------------------------------------------------------
-- 0) CONFIG
-- ---------------------------------------------------------
SET @SELLERS := 1000;          -- 판매자 수
SET @USERS := 100000;          -- 일반 사용자 수
SET @ADMINS := 10;             -- 관리자 수

SET @STORES := 5000;           -- 가게 수
SET @MENUS := 50000;           -- 메뉴 수
SET @ORDERS := 1000000;        -- 주문 수
SET @REVIEWS := 5000000;       -- 리뷰 수 (핵심)

-- ---------------------------------------------------------
-- 1) 숫자 생성용 헬퍼 TABLE
--    0 ~ 999,999 생성 가능
--    ※ TEMPORARY TABLE 대신 일반 TABLE 사용
-- ---------------------------------------------------------
DROP TABLE IF EXISTS seq_1m;
DROP TABLE IF EXISTS seq_10;

CREATE TABLE seq_10 (
                        n INT PRIMARY KEY
);

INSERT INTO seq_10 (n) VALUES
                           (0),(1),(2),(3),(4),(5),(6),(7),(8),(9);

CREATE TABLE seq_1m AS
SELECT
    d0.n
        + 10*d1.n
        + 100*d2.n
        + 1000*d3.n
        + 10000*d4.n
        + 100000*d5.n AS idx
FROM seq_10 d0
         CROSS JOIN seq_10 d1
         CROSS JOIN seq_10 d2
         CROSS JOIN seq_10 d3
         CROSS JOIN seq_10 d4
         CROSS JOIN seq_10 d5;

CREATE INDEX idx_seq_1m_idx ON seq_1m(idx);

-- ---------------------------------------------------------
-- 2) 시작 ID 기록
-- ---------------------------------------------------------
SET @MEMBER_ID_START := IFNULL((SELECT MAX(member_id) FROM members), 0) + 1;
SET @STORE_ID_START  := IFNULL((SELECT MAX(id) FROM stores), 0) + 1;
SET @MENU_ID_START   := IFNULL((SELECT MAX(id) FROM menus), 0) + 1;
SET @ORDER_ID_START  := IFNULL((SELECT MAX(id) FROM orders), 0) + 1;
SET @REVIEW_ID_START := IFNULL((SELECT MAX(id) FROM reviews), 0) + 1;

-- ---------------------------------------------------------
-- 3) MEMBERS 생성
--    seller / user / admin
-- ---------------------------------------------------------

-- 3-1) SELLER
INSERT INTO members (
    member_id, nickname, email, phone_no, address, password,
    total_point, role, total_price_amount, refresh_token, deleted_at,
    created_at, modified_at
)
SELECT

    @MEMBER_ID_START + idx,
    CONCAT('seller', idx + 1),
    CONCAT('seller', idx + 1, '@test.com'),
    CONCAT('010-', LPAD((idx % 10000), 4, '0'), '-', LPAD(((idx * 7) % 10000), 4, '0')),
    CONCAT('서울시 판매자구역 ', idx + 1),
    'pw',
    0,
    'ROLE_SELLER',
    0,
    NULL,
    NULL,
    NOW(),
    NOW()
FROM seq_1m
WHERE idx < @SELLERS;

SET @USER_MEMBER_ID_START := @MEMBER_ID_START + @SELLERS;

-- 3-2) USER
INSERT INTO members (
    member_id, nickname, email, phone_no, address, password,
    total_point, role, total_price_amount, refresh_token, deleted_at,
    created_at, modified_at
)
SELECT
    @USER_MEMBER_ID_START + idx,
    CONCAT('user', idx + 1),
    CONCAT('user', idx + 1, '@test.com'),
    CONCAT('010-', LPAD((idx % 10000), 4, '0'), '-', LPAD(((idx * 11) % 10000), 4, '0')),
    CONCAT('서울시 사용자구역 ', idx + 1),
    'pw',
    (idx % 5000),
    'ROLE_USER',
    0,
    NULL,
    NULL,
    NOW(),
    NOW()
FROM seq_1m
WHERE idx < @USERS;

SET @ADMIN_MEMBER_ID_START := @USER_MEMBER_ID_START + @USERS;

-- 3-3) ADMIN
INSERT INTO members (
    member_id, nickname, email, phone_no, address, password,
    total_point, role, total_price_amount, refresh_token, deleted_at,
    created_at, modified_at
)
SELECT
    @ADMIN_MEMBER_ID_START + idx,
    CONCAT('admin', idx + 1),
    CONCAT('admin', idx + 1, '@test.com'),
    CONCAT('010-9999-', LPAD(idx + 1, 4, '0')),
    CONCAT('서울시 관리자구역 ', idx + 1),
    'pw',
    0,
    'ROLE_ADMIN',
    0,
    NULL,
    NULL,
    NOW(),
    NOW()
FROM seq_1m
WHERE idx < @ADMINS;

-- ---------------------------------------------------------
-- 4) MEMBERS row number 매핑 테이블 생성
--    FK 연결 시 갭이 있어도 안전하게 사용
-- ---------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_sellers;
CREATE TEMPORARY TABLE tmp_sellers AS
SELECT
    member_id,
    ROW_NUMBER() OVER (ORDER BY member_id) AS rn
FROM members
WHERE role = 'ROLE_SELLER';

CREATE INDEX idx_tmp_sellers_rn ON tmp_sellers(rn);

DROP TEMPORARY TABLE IF EXISTS tmp_users;
CREATE TEMPORARY TABLE tmp_users AS
SELECT
    member_id,
    ROW_NUMBER() OVER (ORDER BY member_id) AS rn
FROM members
WHERE role = 'ROLE_USER';

CREATE INDEX idx_tmp_users_rn ON tmp_users(rn);

SET @SELLER_CNT := (SELECT COUNT(*) FROM tmp_sellers);
SET @USER_CNT   := (SELECT COUNT(*) FROM tmp_users);

-- ---------------------------------------------------------
-- 5) STORES 생성
--    seller에게 순환 배정
-- ---------------------------------------------------------
INSERT INTO stores (
    id, store_name, delivery_fee, status, minimum_order, member_id,
    created_at, modified_at
)
SELECT
    @STORE_ID_START + s.idx,
    CONCAT(
            CASE (s.idx % 6)
                WHEN 0 THEN '한식당 '
                WHEN 1 THEN '중식당 '
                WHEN 2 THEN '치킨집 '
                WHEN 3 THEN '분식집 '
                WHEN 4 THEN '피자집 '
                ELSE '일식당 '
                END,
            s.idx + 1
    ),
    2000 + ((s.idx % 5) * 500),
    'OPEN',
    12000 + ((s.idx % 6) * 1000),
    sel.member_id,
    NOW(),
    NOW()
FROM seq_1m s
         JOIN tmp_sellers sel
              ON sel.rn = ((s.idx % @SELLER_CNT) + 1)
WHERE s.idx < @STORES;

DROP TEMPORARY TABLE IF EXISTS tmp_stores;
CREATE TEMPORARY TABLE tmp_stores AS
SELECT
    id,
    ROW_NUMBER() OVER (ORDER BY id) AS rn
FROM stores;

CREATE INDEX idx_tmp_stores_rn ON tmp_stores(rn);

SET @STORE_CNT := (SELECT COUNT(*) FROM tmp_stores);

-- ---------------------------------------------------------
-- 6) MENUS 생성
--    store에 순환 배정
-- ---------------------------------------------------------
INSERT INTO menus (
    id, store_id, name, price, stock, status, created_at, modified_at
)
SELECT
    @MENU_ID_START + m.idx,
    st.id,
    CONCAT(
            CASE (m.idx % 10)
                WHEN 0 THEN '비빔밥 '
                WHEN 1 THEN '김치찌개 '
                WHEN 2 THEN '된장찌개 '
                WHEN 3 THEN '짜장면 '
                WHEN 4 THEN '짬뽕 '
                WHEN 5 THEN '탕수육 '
                WHEN 6 THEN '후라이드치킨 '
                WHEN 7 THEN '양념치킨 '
                WHEN 8 THEN '돈까스 '
                ELSE '제육볶음 '
                END,
            m.idx + 1
    ),
    7000 + ((m.idx % 20) * 500),
    50 + (m.idx % 200),
    'PREPARING',
    NOW(),
    NOW()
FROM seq_1m m
         JOIN tmp_stores st
              ON st.rn = ((m.idx % @STORE_CNT) + 1)
WHERE m.idx < @MENUS;

-- ---------------------------------------------------------
-- 7) ORDERS 생성
--    user, store 매핑
-- ---------------------------------------------------------
INSERT INTO orders (
    id, order_num, total_price, order_status, order_at, used_point,
    user_id, store_id, created_at, modified_at
)
SELECT
    @ORDER_ID_START + o.idx,
    CONCAT('ORD-', LPAD(@ORDER_ID_START + o.idx, 12, '0')),
    15000 + ((o.idx % 15) * 2000),

    CASE
        WHEN o.idx % 25 = 0 THEN 'CANCELLED'
        WHEN o.idx % 15 = 0 THEN 'REFUNDED'
        WHEN o.idx % 10 = 0 THEN 'PREPARING'
        WHEN o.idx % 5  = 0 THEN 'COMPLETED'
        ELSE 'WAITING'
        END,

    DATE_SUB(NOW(), INTERVAL (o.idx % 365) DAY),
    (o.idx % 3000),
    u.member_id,
    s.id,
    DATE_SUB(NOW(), INTERVAL (o.idx % 365) DAY),
    DATE_SUB(NOW(), INTERVAL (o.idx % 365) DAY)

FROM seq_1m o
         JOIN tmp_users u
              ON u.rn = ((o.idx % @USER_CNT) + 1)

         JOIN tmp_stores s
              ON s.rn = (((o.idx * 7) % @STORE_CNT) + 1)

WHERE o.idx < @ORDERS;

DROP TEMPORARY TABLE IF EXISTS tmp_orders;
CREATE TEMPORARY TABLE tmp_orders AS
SELECT
    id,
    user_id,
    store_id,
    ROW_NUMBER() OVER (ORDER BY id) AS rn
FROM orders
WHERE order_status = 'COMPLETED';

CREATE INDEX idx_tmp_orders_rn ON tmp_orders(rn);

SET @COMPLETED_ORDER_CNT := (SELECT COUNT(*) FROM tmp_orders);

-- ---------------------------------------------------------
-- 8) REVIEWS 5,000,000건 생성
--    completed 주문 기준으로 순환 매핑
--    ※ reviews.order_id 가 UNIQUE면 이 방식은 사용 불가
-- ---------------------------------------------------------

-- 8-1) 1,000,000
INSERT INTO reviews (
    id, store_id, order_id, user_id, name, rating, content, status,
    created_at, modified_at
)
SELECT
    @REVIEW_ID_START + r.idx,
    o.store_id,
    o.id,
    o.user_id,
    CONCAT('user', o.user_id),
    1 + (r.idx % 5),
    CASE (r.idx % 10)
        WHEN 0 THEN '정말 맛있어요'
        WHEN 1 THEN '배달이 빨라서 좋았어요'
        WHEN 2 THEN '양이 많고 만족스러워요'
        WHEN 3 THEN '무난하게 먹기 좋았어요'
        WHEN 4 THEN '재주문 의사 있습니다'
        WHEN 5 THEN '조금 짰지만 맛은 있었어요'
        WHEN 6 THEN '포장이 깔끔했어요'
        WHEN 7 THEN '가격 대비 괜찮아요'
        WHEN 8 THEN '다음에는 다른 메뉴도 먹어볼게요'
        ELSE '전반적으로 만족합니다'
        END,
    'EXPOSED',
    DATE_SUB(NOW(), INTERVAL (r.idx % 365) DAY),
    DATE_SUB(NOW(), INTERVAL (r.idx % 365) DAY)
FROM seq_1m r
         JOIN tmp_orders o
              ON o.rn = ((r.idx % @COMPLETED_ORDER_CNT) + 1)
WHERE r.idx < 1000000;

-- 8-2) 2,000,000
INSERT INTO reviews (
    id, store_id, order_id, user_id, name, rating, content, status,
    created_at, modified_at
)
SELECT
    @REVIEW_ID_START + 1000000 + r.idx,
    o.store_id,
    o.id,
    o.user_id,
    CONCAT('user', o.user_id),
    1 + ((1000000 + r.idx) % 5),
    CASE ((1000000 + r.idx) % 10)
    WHEN 0 THEN '정말 맛있어요'
    WHEN 1 THEN '배달이 빨라서 좋았어요'
    WHEN 2 THEN '양이 많고 만족스러워요'
    WHEN 3 THEN '무난하게 먹기 좋았어요'
    WHEN 4 THEN '재주문 의사 있습니다'
    WHEN 5 THEN '조금 짰지만 맛은 있었어요'
    WHEN 6 THEN '포장이 깔끔했어요'
    WHEN 7 THEN '가격 대비 괜찮아요'
    WHEN 8 THEN '다음에는 다른 메뉴도 먹어볼게요'
    ELSE '전반적으로 만족합니다'
END,
    'EXPOSED',
    DATE_SUB(NOW(), INTERVAL ((1000000 + r.idx) % 365) DAY),
    DATE_SUB(NOW(), INTERVAL ((1000000 + r.idx) % 365) DAY)
FROM seq_1m r
         JOIN tmp_orders o
              ON o.rn = (((1000000 + r.idx) % @COMPLETED_ORDER_CNT) + 1)
WHERE r.idx < 1000000;

-- 8-3) 3,000,000
INSERT INTO reviews (
    id, store_id, order_id, user_id, name, rating, content, status,
    created_at, modified_at
)
SELECT
    @REVIEW_ID_START + 2000000 + r.idx,
    o.store_id,
    o.id,
    o.user_id,
    CONCAT('user', o.user_id),
    1 + ((2000000 + r.idx) % 5),
    CASE ((2000000 + r.idx) % 10)
    WHEN 0 THEN '정말 맛있어요'
    WHEN 1 THEN '배달이 빨라서 좋았어요'
    WHEN 2 THEN '양이 많고 만족스러워요'
    WHEN 3 THEN '무난하게 먹기 좋았어요'
    WHEN 4 THEN '재주문 의사 있습니다'
    WHEN 5 THEN '조금 짰지만 맛은 있었어요'
    WHEN 6 THEN '포장이 깔끔했어요'
    WHEN 7 THEN '가격 대비 괜찮아요'
    WHEN 8 THEN '다음에는 다른 메뉴도 먹어볼게요'
    ELSE '전반적으로 만족합니다'
END,
    'EXPOSED',
    DATE_SUB(NOW(), INTERVAL ((2000000 + r.idx) % 365) DAY),
    DATE_SUB(NOW(), INTERVAL ((2000000 + r.idx) % 365) DAY)
FROM seq_1m r
         JOIN tmp_orders o
              ON o.rn = (((2000000 + r.idx) % @COMPLETED_ORDER_CNT) + 1)
WHERE r.idx < 1000000;

-- 8-4) 4,000,000
INSERT INTO reviews (
    id, store_id, order_id, user_id, name, rating, content, status,
    created_at, modified_at
)
SELECT
    @REVIEW_ID_START + 3000000 + r.idx,
    o.store_id,
    o.id,
    o.user_id,
    CONCAT('user', o.user_id),
    1 + ((3000000 + r.idx) % 5),
    CASE ((3000000 + r.idx) % 10)
    WHEN 0 THEN '정말 맛있어요'
    WHEN 1 THEN '배달이 빨라서 좋았어요'
    WHEN 2 THEN '양이 많고 만족스러워요'
    WHEN 3 THEN '무난하게 먹기 좋았어요'
    WHEN 4 THEN '재주문 의사 있습니다'
    WHEN 5 THEN '조금 짰지만 맛은 있었어요'
    WHEN 6 THEN '포장이 깔끔했어요'
    WHEN 7 THEN '가격 대비 괜찮아요'
    WHEN 8 THEN '다음에는 다른 메뉴도 먹어볼게요'
    ELSE '전반적으로 만족합니다'
END,
    'EXPOSED',
    DATE_SUB(NOW(), INTERVAL ((3000000 + r.idx) % 365) DAY),
    DATE_SUB(NOW(), INTERVAL ((3000000 + r.idx) % 365) DAY)
FROM seq_1m r
         JOIN tmp_orders o
              ON o.rn = (((3000000 + r.idx) % @COMPLETED_ORDER_CNT) + 1)
WHERE r.idx < 1000000;

-- 8-5) 5,000,000
INSERT INTO reviews (
    id, store_id, order_id, user_id, name, rating, content, status,
    created_at, modified_at
)
SELECT
    @REVIEW_ID_START + 4000000 + r.idx,
    o.store_id,
    o.id,
    o.user_id,
    CONCAT('user', o.user_id),
    1 + ((4000000 + r.idx) % 5),
    CASE ((4000000 + r.idx) % 10)
    WHEN 0 THEN '정말 맛있어요'
    WHEN 1 THEN '배달이 빨라서 좋았어요'
    WHEN 2 THEN '양이 많고 만족스러워요'
    WHEN 3 THEN '무난하게 먹기 좋았어요'
    WHEN 4 THEN '재주문 의사 있습니다'
    WHEN 5 THEN '조금 짰지만 맛은 있었어요'
    WHEN 6 THEN '포장이 깔끔했어요'
    WHEN 7 THEN '가격 대비 괜찮아요'
    WHEN 8 THEN '다음에는 다른 메뉴도 먹어볼게요'
    ELSE '전반적으로 만족합니다'
END,
    'EXPOSED',
    DATE_SUB(NOW(), INTERVAL ((4000000 + r.idx) % 365) DAY),
    DATE_SUB(NOW(), INTERVAL ((4000000 + r.idx) % 365) DAY)
FROM seq_1m r
         JOIN tmp_orders o
              ON o.rn = (((4000000 + r.idx) % @COMPLETED_ORDER_CNT) + 1)
WHERE r.idx < 1000000;

-- ---------------------------------------------------------
-- 9) 검증
-- ---------------------------------------------------------
SELECT COUNT(*) AS total_members FROM members;
SELECT COUNT(*) AS total_stores  FROM stores;
SELECT COUNT(*) AS total_menus   FROM menus;
SELECT COUNT(*) AS total_orders  FROM orders;
SELECT COUNT(*) AS total_reviews FROM reviews;

SELECT MIN(id) AS review_start_id, MAX(id) AS review_end_id
FROM reviews
WHERE id >= @REVIEW_ID_START;
