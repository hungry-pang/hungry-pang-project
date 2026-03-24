package com.example.hungrypangproject.domain.membership.repository;

import com.example.hungrypangproject.domain.membership.entity.GradeEnum;
import com.example.hungrypangproject.domain.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByGrade (GradeEnum grade);
}
