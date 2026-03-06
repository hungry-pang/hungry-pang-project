package com.example.hungrypangproject.domain.member.entity;

import com.example.hungrypangproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String phoneNo;

    @Column(nullable = false, length = 225)
    private String address;

    @Column(nullable = false, length = 50)
    private String password;

    @Column(nullable = false)
    private Long point;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    private LocalDateTime modifiedAt;

    public static Member register(
            String nickname,
            String emil,
            String phoneNo,
            String address,
            String password,
            Integer point
    ) {
        Member member = new Member();

        member.nickname = nickname;
        member.email = emil;
        member.phoneNo = phoneNo;
        member.address = address;
        member.password = password;
        member.point = 0L;
        member.deletedAt = null;

        return member;
    }

}
