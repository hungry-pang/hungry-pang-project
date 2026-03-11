package com.example.hungrypangproject.domain.member.repository;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일 중복 체크
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);

    List<Member> findAllByRole(MemberRoleEnum role);

}
