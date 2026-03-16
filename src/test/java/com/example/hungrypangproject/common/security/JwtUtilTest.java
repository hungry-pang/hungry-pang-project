package com.example.hungrypangproject.common.security;

import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secretKey = "ZGtjbmxka24xMzVkbGtuY2tkbmZqNDM5NzNkbGtuY2syZGxja25kazkzOGVuZjJsa2Zua2RuY2oyMjM0MmxrZG5sd2tyb20=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // @Value로 들어가는 값을 수동으로 설정 (Reflection 사용)
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secretKeyString", encodedKey);

        // @PostConstruct 메서드를 직접 호출해서 초기화
        jwtUtil.init();
    }

    @Test
    @DisplayName("토큰 생성 및 이메일 추출 성공")
    void createAndExtractTest() {
        // given
        String email = "test@test.com";
        MemberRoleEnum role = MemberRoleEnum.ROLE_USER;

        // when
        String token = jwtUtil.createAccessToken(email, role);
        String pureToken = jwtUtil.substringToken(token); // Bearer 제거
        String extractedEmail = jwtUtil.extractEmail(pureToken);

        // then
        assertThat(token).startsWith("Bearer ");
        assertThat(extractedEmail).isEqualTo(email);
        assertThat(jwtUtil.validateToken(pureToken)).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 시 false 반환")
    void validateToken_Fail() {
        // given
        String invalidToken = "wrong.token.value";

        // when
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("RefreshToken 여부 확인")
    void isRefreshTokenTest() {
        // given
        String email = "refresh@test.com";
        String refreshToken = jwtUtil.createRefreshToken(email);
        String pureToken = jwtUtil.substringToken(refreshToken);

        // when & then
        assertThat(jwtUtil.isRefreshToken(pureToken)).isTrue();
    }

    @Test
    @DisplayName("토큰이 null이면 에러가 발생한다")
    void substringToken_Null_Fail() {
        // given
        String nullToken = null; // 진짜 null을 준비

        // when & then 실행과 동시에 검증
        org.assertj.core.api.Assertions.assertThatThrownBy(
                ()-> {
                    jwtUtil.substringToken(nullToken);
                })
                .isInstanceOf(NullPointerException.class) // 에러 종류 확인
                .hasMessageContaining("토큰이 없거나 유효하지 않습니다"); // 에러 메시지 확인
    }

    @Test
    @DisplayName("Bearer로 시작하지 않는 토큰은 에러가 발생한다")
    void substringToken_InvalidPrefix_Fail() {
        // given
        String noBearerToken = "abc12345.token.value";

        // when & then
        assertThatThrownBy(()-> jwtUtil.substringToken(noBearerToken))
                .isInstanceOf(NullPointerException.class);
    }

}
