package com.example.hungrypangproject.domain.member.service;

import com.example.hungrypangproject.common.exception.ErrorCode;
import com.example.hungrypangproject.common.exception.ServiceException;
import com.example.hungrypangproject.common.security.JwtUtil;
import com.example.hungrypangproject.domain.member.dto.request.*;
import com.example.hungrypangproject.domain.member.dto.respons.*;
import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "MemberService")
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // 회원가입
    @Transactional
    public SaveMemberResponse signup (SaveMemberRequest request) {
        if(memberRepository.existsByEmail(request.getEmail())) {
            throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodePassword = passwordEncoder.encode(request.getPassword());
        Member member = Member.register(request, encodePassword);

        memberRepository.save(member);
        return SaveMemberResponse.register(member);
    }

    // 로그인
    @Transactional
    public LoginInfo login (LoginMemberRequest request) {
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

        // Refresh token DB 저장 (로그아웃 또는 재발급 시 검증용)
        member.updateRefreshToken(refreshToken);

        return LoginInfo.register(userDetails.getMember(),accessToken, refreshToken);
    }

    // refresh token 발행
    @Transactional
    public LoginInfo refresh(String refreshToken){

        // Bearer 제거
        String token = jwtUtil.substringToken(refreshToken);

        // jwtUtil 인스턴스를 사용한 유효성 검사
        if(!jwtUtil.validateToken(token)) {
            throw new ServiceException(ErrorCode.INVALID_TOKEN);
        }

        // 유저 찾기 및 DB 리프레시 토큰 대조
        String email = jwtUtil.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        if(!token.equals(member.getRefreshToken())) {
            throw new ServiceException(ErrorCode.INVALID_TOKEN);
        }

        // 새로운 토큰 생성
        String newAccess = jwtUtil.createAccessToken(member.getEmail(),member.getRole());
        String newRefresh = jwtUtil.createRefreshToken(member.getEmail());

        member.updateRefreshToken(newRefresh);

        return LoginInfo.register(member, newAccess, newRefresh);
    }

    // 회원정보 수정
    @Transactional
    public UpdateMemberResponse updateMemberResponse(Member member, UpdateMemberRequest request){
       member.updateInfo(request.getNickname(), request.getAddress(),request.getPhoneNo());

       return UpdateMemberResponse.register(member);
    }

    // 회원정보 조회
    @Transactional(readOnly = true)
    public SearchMemberResponse findOne(Member member) {
        return SearchMemberResponse.register(member);
    }

    // 로그아웃
    @Transactional
    public void logout(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateRefreshToken(null);
        log.info("{} 사용자가 로그아웃되었습니다", member.getEmail());
    }

}
