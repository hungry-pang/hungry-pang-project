-- orders.order_status enum 정리 스크립트
-- 목적: 레거시 오타 WATING -> WAITING 치환 및 CANCELLED 상태 추가

USE test;

ALTER TABLE orders
    MODIFY COLUMN order_status ENUM('WAITING','WATING','PREPARING','COMPLETED','REFUNDED','CANCELLED') NOT NULL;

UPDATE orders
SET order_status = 'WAITING'
WHERE order_status = 'WATING';

ALTER TABLE orders
    MODIFY COLUMN order_status ENUM('WAITING','PREPARING','COMPLETED','REFUNDED','CANCELLED') NOT NULL;

