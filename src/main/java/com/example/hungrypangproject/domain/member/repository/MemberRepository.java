package com.example.hungrypangproject.domain.member.repository;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
    List<Member> findAllByRole(MemberRoleEnum role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId")
    Optional<Member> findByaMemberIdForLock(@Param("memberId") Long memberId);

}
