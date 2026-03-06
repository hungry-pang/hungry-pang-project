package com.example.hungrypangproject.domain.membership.repository;

import com.example.hungrypangproject.domain.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
}
