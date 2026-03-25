-- members 테이블 포인트 컬럼 정리 스크립트
-- 목적: point / points / total_point 혼재로 회원가입 시 NOT NULL 오류가 발생하는 문제 해결

USE test;

-- 1) canonical 컬럼 보장
ALTER TABLE members
    ADD COLUMN IF NOT EXISTS total_point DECIMAL(19, 2) NOT NULL DEFAULT 0;

-- 2) 기존 레거시 컬럼 값 병합
--    아래 UPDATE는 point/points 컬럼이 없는 환경에서는 오류가 날 수 있으므로
--    DB 콘솔에서 존재하는 컬럼만 남겨 실행하세요.
UPDATE members
SET total_point = COALESCE(total_point, point, points, 0);

-- 3) total_point 제약 보장
ALTER TABLE members
    MODIFY COLUMN total_point DECIMAL(19, 2) NOT NULL DEFAULT 0;

-- 4) 레거시 컬럼 정리(존재할 때만 실행)
ALTER TABLE members DROP COLUMN IF EXISTS point;
ALTER TABLE members DROP COLUMN IF EXISTS points;

