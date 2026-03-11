package com.example.hungrypangproject;

import com.example.hungrypangproject.domain.member.entity.Member;
import com.example.hungrypangproject.domain.member.entity.MemberRoleEnum;
import com.example.hungrypangproject.domain.member.repository.MemberRepository;
import com.example.hungrypangproject.domain.menu.entity.Menu;
import com.example.hungrypangproject.domain.menu.entity.MenuStatus;
import com.example.hungrypangproject.domain.menu.repository.MenuRepository;
import com.example.hungrypangproject.domain.store.entity.Store;
import com.example.hungrypangproject.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (memberRepository.count() > 0) return; // 중복 삽입 방지

        String encodedPassword = passwordEncoder.encode("test1234");

        // 멤버 생성
        List<Member> members = memberRepository.saveAll(List.of(
                Member.builder()
                        .nickname("라이더1").email("raider1@test.com").phoneNo("010-1111-2222")
                        .address("서울시 강남구").password(encodedPassword)
                        .totalPoint(0L).role(MemberRoleEnum.ROLE_RAIDER).totalPriceAmount(0L)
                        .build(),
                Member.builder()
                        .nickname("라이더2").email("raider2@test.com").phoneNo("010-2222-3333")
                        .address("서울시 서초구").password(encodedPassword)
                        .totalPoint(0L).role(MemberRoleEnum.ROLE_RAIDER).totalPriceAmount(0L)
                        .build(),
                Member.builder()
                        .nickname("라이더3").email("raider3@test.com").phoneNo("010-3333-4444")
                        .address("서울시 마포구").password(encodedPassword)
                        .totalPoint(0L).role(MemberRoleEnum.ROLE_RAIDER).totalPriceAmount(0L)
                        .build(),
                Member.builder()
                        .nickname("셀러1").email("seller1@test.com").phoneNo("010-4444-5555")
                        .address("서울시 종로구").password(encodedPassword)
                        .totalPoint(0L).role(MemberRoleEnum.ROLE_SELLER).totalPriceAmount(0L)
                        .build(),
                Member.builder()
                        .nickname("셀러2").email("seller2@test.com").phoneNo("010-5555-6666")
                        .address("서울시 중구").password(encodedPassword)
                        .totalPoint(0L).role(MemberRoleEnum.ROLE_SELLER).totalPriceAmount(0L)
                        .build(),
                Member.builder()
                        .nickname("셀러3").email("seller3@test.com").phoneNo("010-6666-7777")
                        .address("서울시 용산구").password(encodedPassword)
                        .totalPoint(0L).role(MemberRoleEnum.ROLE_SELLER).totalPriceAmount(0L)
                        .build()
        ));

        Member seller1 = members.get(3); // 셀러1
        Member seller2 = members.get(4); // 셀러2
        Member seller3 = members.get(5); // 셀러3

        // 스토어 생성
        List<Store> stores = storeRepository.saveAll(List.of(
                Store.create("테스트식당1", BigDecimal.valueOf(3000), BigDecimal.valueOf(10000), seller1),
                Store.create("테스트식당2", BigDecimal.valueOf(2000), BigDecimal.valueOf(15000), seller2),
                Store.create("테스트식당3", BigDecimal.valueOf(0),    BigDecimal.valueOf(0),     seller3)
        ));

        Store store1 = stores.get(0);
        Store store2 = stores.get(1);
        Store store3 = stores.get(2);

        // 메뉴 생성
        menuRepository.saveAll(List.of(
                Menu.create(store1, "후라이드치킨", BigDecimal.valueOf(18000), 100L, MenuStatus.SALE),
                Menu.create(store1, "양념치킨",   BigDecimal.valueOf(19000), 100L, MenuStatus.SALE),
                Menu.create(store2, "짜장면",     BigDecimal.valueOf(8000),  50L,  MenuStatus.SALE),
                Menu.create(store2, "짬뽕",       BigDecimal.valueOf(9000),  50L,  MenuStatus.SALE),
                Menu.create(store3, "김치찌개",   BigDecimal.valueOf(7000),  30L,  MenuStatus.SALE),
                Menu.create(store3, "된장찌개",   BigDecimal.valueOf(7000),  30L,  MenuStatus.SALE)
        ));
    }
}