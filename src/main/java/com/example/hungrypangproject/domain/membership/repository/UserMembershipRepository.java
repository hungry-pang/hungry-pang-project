package com.example.hungrypangproject.domain.membership.repository;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.membership.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository <UserMembership, Long> {

    Optional<UserMembership> findByMember(Member member);
}
