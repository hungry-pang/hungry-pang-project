package com.example.hungrypangproject.domain.member.controller;

import com.example.hungrypangproject.domain.member.dto.request.*;
import com.example.hungrypangproject.domain.member.dto.respons.*;
import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    /*
     * 1. 회원가입 (JWT 통과)
     * 2. 로그인 (JWT 통과)
     * 3. RefreshToke 새로운 토큰 발급 (JWT 통과)
     * 4. 회원정보 조회
     * 5. 회원정보 수정
     * 5. 역할 상태 변경
     * 7. 로그아웃
     */

    @PostMapping("/signup")
    public ResponseEntity<SaveMemberResponse> signup (
            @Valid @RequestBody SaveMemberRequest request
            ) {
        SaveMemberResponse response = memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginMemberResponse> login (
            @Valid @RequestBody LoginMemberRequest request
            ) {
        LoginInfo info = memberService.login(request);

        // LoginInfo에서 토큰을 포스트타입 headers에 담아 응답
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", info.getAccessToken());
        headers.set("Refresh-Token", info.getRefreshToken());

        // LoginInfo에서 로그인 정보를 토대로 response 객체 생성 후 응답
        LoginMemberResponse response = LoginMemberResponse.register(info);
        return ResponseEntity.ok().headers(headers).body(response);
    }

    // RefreshToken 새로운 토큰 발급
    @GetMapping("/refresh")
    public ResponseEntity<LoginInfo> refresh (
            @RequestHeader("Refresh-Token") String refreshToken
    ){
        // refresh token으로 정보를 다시 가져옴
        LoginInfo info = memberService.refresh(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", info.getAccessToken());
        headers.set("Refresh-Token", info.getRefreshToken());

        return ResponseEntity.ok().headers(headers).body(info);
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<SearchMemberResponse> getId(
            @PathVariable Long memberId
    ) {
        SearchMemberResponse response = memberService.findOne(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/members/{memberId}")
    public ResponseEntity<UpdateMemberResponse> updateMember (
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(memberService.update(memberId,request));
    }

    @PatchMapping("/members/{memberId}/role")
    public ResponseEntity<UpdateMemberResponse> updateMemberRole (
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(memberService.updateMemberRole(memberId, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String accessToken,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ){
        memberService.logout(accessToken, userDetails.getMember().getEmail());
        return ResponseEntity.ok().build();
    }
}
