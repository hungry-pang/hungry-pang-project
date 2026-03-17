package com.example.hungrypangproject.domain.member.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.common.security.RedisUtil;
import com.example.hungrypangproject.common.security.JwtUtil;
import com.example.hungrypangproject.domain.member.dto.request.*;
import com.example.hungrypangproject.domain.member.dto.respons.*;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j(topic = "MemberService")
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MembershipService membershipService;
    private final RedisUtil redisUtil;
    private final MemberCacheService memberCacheService;

    /*
     * 1. 회원가입 : 회원가입 동시에 멤버십 등급 NORMAL 초기화
     * 2. 로그인 : AccessToken, RefreshToken 발급
     * 3. RefreshToke 재발급  -> 수정 필요
     * 4. 회원정보 조회
     * 5. 회원정보 수정
     * 6. 역할 상태 변경
     * 7. 로그아웃
     * 8. 캐시 조회
     */

    @Transactional
    public SaveMemberResponse signup(SaveMemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodePassword = passwordEncoder.encode(request.getPassword());
        Member member = Member.register(request, encodePassword);

        memberRepository.save(member);

        membershipService.setupMembership(member);
        log.info("회원가입 및 멤버십 초기화 완료: {}", member.getEmail());

        return SaveMemberResponse.register(member);
    }

    @Transactional
    public LoginInfo login(LoginMemberRequest request) {
        // authentication 객체 생성 및 인증 로직 진행
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // authentication 객체에서 memberUserDetails 추출
        MemberUserDetails userDetails = (MemberUserDetails) authentication.getPrincipal();
        Member member = userDetails.getMember();

        // Access token 및 Refresh token 생성
        String accessToken = jwtUtil.createAccessToken(member.getEmail(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getEmail());

        // Redis에 RefreshToken 저장
        long refreshExpiration = jwtUtil.getRefreshExpiration();
        memberCacheService.saveRefreshToken(member.getEmail(), refreshToken, refreshExpiration);

        // Bearer 제거
        String pureNewRefresh = jwtUtil.substringToken(refreshToken);

        // Refresh token DB 저장 (로그아웃 또는 재발급 시 검증용)
        member.updateRefreshToken(pureNewRefresh);

        return LoginInfo.register(member, accessToken, refreshToken);
    }

    @Transactional
    public LoginInfo refresh(String refreshToken) {

        // Bearer 제거
        String token = jwtUtil.substringToken(refreshToken);

        // jwtUtil 인스턴스를 사용한 유효성 검사
        if (!jwtUtil.validateToken(token)) {
            throw new ServiceException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // Redis 에서 유저 찾기 및 리프레시 토큰 대조
        String email = jwtUtil.extractEmail(token);
        String savedToken = memberCacheService.getRefreshToken(email);

        if (savedToken == null) {
            throw new ServiceException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        log.info("===========[재발급 프로세스 시작]===========");
        log.info("재발급 시도 유저: {}", email);
        log.info("Redis에 저장된 RT와 일치 여부: {}", refreshToken.equals(savedToken));

        if (!savedToken.equals(refreshToken)) {
            throw new ServiceException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 새로운 Access 및 Refresh 토큰 생성
        String newAccess = jwtUtil.createAccessToken(member.getEmail(), member.getRole());
        String newRefresh = jwtUtil.createRefreshToken(member.getEmail());

        // Redis에 새로운 RefreshToken 업데이트 (기존 토큰 덮어쓰기)
        long refreshExpiration = jwtUtil.getRefreshExpiration();
        memberCacheService.saveRefreshToken(email, newRefresh, refreshExpiration);

        log.info("새로운 RT로 Redis 업데이트 완료");
        log.info("===========[재발급 프로세스 종료]===========");

        return LoginInfo.register(member, newAccess, newRefresh);
    }

    @Transactional(readOnly = true)
    public SearchMemberResponse findOne(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        return SearchMemberResponse.register(member);
    }

    @Transactional
    public UpdateMemberResponse update(Long memberId, UpdateMemberRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateInfo(
                request.getNickname(),
                request.getAddress(),
                request.getPhoneNo()
        );

        return UpdateMemberResponse.register(member);
    }

    @Transactional
    public UpdateMemberResponse updateMemberRole(Long memberId, UpdateMemberRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateRole(request.getRole());

        return UpdateMemberResponse.register(member);
    }

    @Transactional
    public void logout(String accessToken, String email) {

        String token = jwtUtil.substringToken(accessToken);
        memberCacheService.deleteRefreshToken(email);

        try {
            long expiration = jwtUtil.getExpiration(token);
            log.info("===[디버깅] 추출된 만료 시간 숫자: {} ===",expiration);

            if (expiration > 0) {
                redisUtil.setBlackList(token, "Logout", Duration.ofMillis(expiration));
                log.info("[블랙리스트 등록] 사용자가 로그아웃하였습니다. 남은 시간: {}ms", expiration);
            }
        } catch (Exception e){
            log.info("이미 만료된 토큰입니다.");
            }
        }
    }