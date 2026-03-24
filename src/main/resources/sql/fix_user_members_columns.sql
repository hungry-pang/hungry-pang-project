-- user_members 테이블 컬럼 정리 스크립트
-- 목적: total_amount / total_price, member_id / members, membership_id / memberships 혼재 해결

USE test;

-- 1) canonical 컬럼 기준으로 값 병합
UPDATE user_members
SET member_id = COALESCE(member_id, members);

UPDATE user_members
SET membership_id = COALESCE(membership_id, memberships);

UPDATE user_members
SET total_amount = COALESCE(total_amount, total_price, 0);

-- 2) canonical 컬럼 제약 보장
ALTER TABLE user_members
    MODIFY COLUMN member_id BIGINT NOT NULL;

ALTER TABLE user_members
    MODIFY COLUMN membership_id BIGINT NOT NULL;

ALTER TABLE user_members
    MODIFY COLUMN total_amount DECIMAL(38, 2) NOT NULL DEFAULT 0;

-- 3) 레거시 FK/UNIQUE 제약 제거 후 컬럼 정리
-- 환경마다 제약 이름이 다를 수 있어, 아래 이름은 SHOW CREATE TABLE 결과에 맞춰 조정하세요.
ALTER TABLE user_members DROP FOREIGN KEY FK171mvumt7b3iyb6djenanklbt;
ALTER TABLE user_members DROP FOREIGN KEY FKjcvn75mp4v4j5fpm27ulgva1h;
ALTER TABLE user_members DROP INDEX UK2d7fl6hadgiikmjupwfjiiviq;

ALTER TABLE user_members DROP COLUMN IF EXISTS members;
ALTER TABLE user_members DROP COLUMN IF EXISTS memberships;
ALTER TABLE user_members DROP COLUMN IF EXISTS total_price;


