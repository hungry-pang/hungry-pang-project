INSERT INTO members (nickname, email, phone_no, address, password, point, deleted_at, created_at, modified_at)
VALUES ('테스트유저', 'test@test.com', '010-1234-5678', '서울시 강남구', '1234', 1000000, NULL, NOW(), NOW());

-- stores
INSERT INTO stores (store_name, delivery_fee, status, minimum_order, created_at, modified_at)
VALUES ('테스트식당', 3000, 'OPEN', 10000, NOW(), NOW());

-- menus
INSERT INTO menus (store_id, name, price, stock, status, created_at, modified_at)
VALUES (1, '테스트메뉴1', 10000, 100, 'SALE', NOW(), NOW()),
       (1, '테스트메뉴2', 15000, 100, 'SALE', NOW(), NOW());