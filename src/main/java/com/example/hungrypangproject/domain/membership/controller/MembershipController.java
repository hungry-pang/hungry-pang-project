package com.example.hungrypangproject.domain.membership.controller;

import com.example.hungrypangproject.domain.member.entity.MemberUserDetails;
import com.example.hungrypangproject.domain.membership.dto.MembershipResponse;
import com.example.hungrypangproject.domain.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/membership/{memberId}")
    public ResponseEntity<MembershipResponse> myMembership(
            @AuthenticationPrincipal MemberUserDetails userDetails
    ){
        MembershipResponse response = membershipService.getMembership(userDetails.getMember());
        return ResponseEntity.ok(response);
    }
}
