package com.example.hungrypangproject.domain.member.repository;

import com.example.hungrypangproject.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
