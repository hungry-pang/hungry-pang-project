import http from 'k6/http';
import { check, sleep } from 'k6';

/* k6 부하 테스트
 * Redis Cache 적용한 검색 API에 대한
 * v1, v2 API 부하 테스트 파일입니다.
 *
 * 실행 방법 (터미널) : k6 run src/main/resources/load-test.js
 */

// 50명의 사용자가 30초 동안 미친듯이 새로고침(조회)을 하는 상황
export let options = {
    vus: 50,
    duration: '30s',
};

export default function () {
    // 🚨 여기에 포스트맨에서 발급받은 실제 토큰 넣기
    const token = 'Bearer 뺀 로그인 발행 토큰';

    const params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };

    // 테스트할 회원 ID
    const targetMemberId = 1;

    // [테스트 A] V1: 기존 DB 직접 조회
    const res = http.get(`http://127.0.0.1:8080/api/v1/members/${targetMemberId}`, params);

    // [테스트 B] V2: Redis 캐시 조회
    // const res = http.get(`http://127.0.0.1:8080/api/v2/members/${targetMemberId}`, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.log(`에러 발생! 상태 코드: ${res.status}, 에러 내용: ${res.error_code}`);
    }

    sleep(0.01);
}