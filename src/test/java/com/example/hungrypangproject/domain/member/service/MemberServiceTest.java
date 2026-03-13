package com.example.hungrypangproject.domain.member.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.common.security.JwtUtil;
import com.example.hungrypangproject.domain.member.dto.request.LoginMemberRequest;
import com.example.hungrypangproject.domain.member.dto.request.SaveMemberRequest;
import com.example.hungrypangproject.domain.member.dto.respons.LoginInfo;
import com.example.hungrypangproject.domain.member.dto.respons.SaveMemberResponse;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.membership.service.MembershipService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MembershipService membershipService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private MemberService memberService;

    // 회원가입 테스트 (성공 케이스)
    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // given

        SaveMemberRequest request = new SaveMemberRequest(
                "테스트",
                "test@test.com",
                "강남구",
                "010-0000-0000",
                "12345678",
                MemberRoleEnum.ROLE_USER,
                BigDecimal.valueOf(100));
        given(memberRepository.existsByEmail(request.getEmail())).willReturn(false); // 중복 아님
        given(passwordEncoder.encode(request.getPassword())).willReturn("encoded_password");

        // when
        SaveMemberResponse response = memberService.signup(request);

        // then
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        // 실제로 저장이 한 번 호출되었는지 검증
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    // 회원가입 테스트 (이미 존재하는 이메일 실패 케이스)
    @Test
    @DisplayName("중복 이메일 회원가입 시 예외 발생")
    void signup_Fail_DuplicateEmail() {
        // given
        SaveMemberRequest request = new SaveMemberRequest(
                "테스트",
                "test2@exmple.com",
                "강남구",
                "010-0000-0000",
                "12345678",
                MemberRoleEnum.ROLE_USER,
                BigDecimal.valueOf(100));
        given(memberRepository.existsByEmail(anyString())).willReturn(true); // 무조건 중복이라고 설정

        // when & then
        assertThatThrownBy(() -> memberService.signup(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
    }

    // 로그인 테스트 (AuthenticationManager 가짜 인증 처리)
    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginMemberRequest request = new LoginMemberRequest("test@test.com", "12345678");
        Member member = Member.builder() // 빌더가 있다고 가정
                .email("test@test.com")
                .role(MemberRoleEnum.ROLE_USER)
                .build();

        // Security 인증 과정 시뮬레이션
        MemberUserDetails userDetails = new MemberUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtUtil.createAccessToken(anyString(), any())).willReturn("access_token");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refresh_token");

        // when
        LoginInfo result = memberService.login(request);

        // then
        assertThat(result.getAccessToken()).isEqualTo("access_token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh_token");
    }

    //  리프레시 토큰 테스트
    @Test
    @DisplayName("리프레시 토큰 재발급 성공")
    void refresh_Success() {
        // given
        String oldRefreshTokenWithBearer = "Bearer old-refresh-token";
        String pureToken = "old-refresh-token";
        String email = "test@test.com";
        String newAccessToken = "Bearer new-access-token";
        String newRefreshToken = "Bearer new-refresh-token";
        String pureNewToken = "new-refresh-token";

        // 테스트용 멤버 객체 (DB에 저장된 토큰 정보 포함)
        Member member = Member.builder()
                .email(email)
                .role(MemberRoleEnum.ROLE_USER)
                .build();
        // 멤버 객체에 리프레시 토큰이 저장되어 있다고 가정
        member.updateRefreshToken(pureToken);

        // Mock 시뮬레이션 시작
        given(jwtUtil.substringToken(oldRefreshTokenWithBearer)).willReturn(pureToken);
        given(jwtUtil.validateToken(pureToken)).willReturn(true);
        given(jwtUtil.extractEmail(pureToken)).willReturn(email);
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

        // 새 토큰 생성 지시
        given(jwtUtil.createAccessToken(anyString(), any())).willReturn(newAccessToken);
        given(jwtUtil.createRefreshToken(anyString())).willReturn(newRefreshToken);
        given(jwtUtil.substringToken(newRefreshToken)).willReturn(pureNewToken);

        // when)
        LoginInfo result = memberService.refresh(oldRefreshTokenWithBearer);

        // then)
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);

        // DB(멤버 객체)의 리프레시 토큰이 새것으로 업데이트 되었는지 확인
        assertThat(member.getRefreshToken()).isEqualTo(pureNewToken);
    }

    @Test
    @DisplayName("DB의 리프레시 토큰과 일치하지 않으면 예외 발생")
    void refresh_Fail_TokenMismatch() {
        // given
        String requestToken = "Bearer wrong-token";
        String pureToken = "wrong-token";
        String email = "test@test.com";

        Member member = Member.builder()
                .email(email)
                .build();
        member.updateRefreshToken("original-token-in-db"); // DB에는 다른 토큰이 있음

        given(jwtUtil.substringToken(anyString())).willReturn(pureToken);
        given(jwtUtil.validateToken(anyString())).willReturn(true);
        given(jwtUtil.extractEmail(anyString())).willReturn(email);
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> memberService.refresh(requestToken))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getMessage());
    }
}
