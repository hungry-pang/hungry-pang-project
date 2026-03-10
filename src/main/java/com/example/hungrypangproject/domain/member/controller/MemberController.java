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

    // RefreshToken
    @GetMapping("/refresh")
    public ResponseEntity<LoginInfo> refresh (
            @RequestHeader("Refresh-Token") String refreshToken
    ){
        // refresh token으로 정보를 다시 가져옴
        LoginInfo info = memberService.refresh(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", info.getAccessToken());
        headers.set("Refresh-Token", info.getRefreshToken());

        return ResponseEntity.ok().headers(headers).build();
    }

    // 회원정보 조회
    @GetMapping("/members/{memberId}")
    public ResponseEntity<SearchMemberResponse> getId(
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        SearchMemberResponse response = memberService.findOne(userDetails.getMember());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal MemberUserDetails userDetails
    ){
        memberService.logout(userDetails.getMember().getMemberId());
        return ResponseEntity.ok().build();
    }
}
