package com.example.study.repository;

import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.example.study.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.study.entity.QMember.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {
    
    @Autowired // test 클래스는 생성자 주입이 안 되므로 Autowired 사용
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;
    
    @Autowired
    EntityManager em; // JPA 관리 핵심 객체
    
    // QueryDSL로 쿼리문을 작성하기 위한 핵심 객체
    JPAQueryFactory factory;

    @BeforeEach
    void settingObject() {
        factory = new JPAQueryFactory(em); 
        // Autowired가 안 되어서 따로 클래스 생성해야 함
        // 나중엔 config 파일 만들어서 bean 등록 해줄 예정
    }

    void testInsertData() {

        Team teamA = Team.builder()
                .name("teamA")
                .build();
        Team teamB = Team.builder()
                .name("teamB")
                .build();

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = Member.builder()
                .userName("member1")
                .age(10)
                .team(teamA)
                .build();
        Member member2 = Member.builder()
                .userName("member2")
                .age(20)
                .team(teamA)
                .build();
        Member member3 = Member.builder()
                .userName("member3")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = Member.builder()
                .userName("member4")
                .age(40)
                .team(teamB)
                .build();

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
    }

    @Test
    @DisplayName("test JPA")
    void testJPA() {
        List<Member> members = memberRepository.findAll();
        members.forEach(System.out::println);
    }

    @Test
    @DisplayName("test JPQL")
    void testJPQL() {
        // JPQL 사용 방법(생 JPA 사용 시)

        // given
        String jpqlQuery = "SELECT m FROM Member m WHERE m.userName = :userName";

        // when
        Member foundMember = em.createQuery(jpqlQuery, Member.class) // 매개값1: JPQL, 매개값2: 리턴타입
                .setParameter("userName", "member2")
                .getSingleResult();

        // then
        assertEquals("teamA", foundMember.getTeam().getName());

        System.out.println("\n\n\n");
        System.out.println("foundMember = " + foundMember);
        System.out.println("foundMember.getTeam = " + foundMember.getTeam());
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("TestQueryDSL")
    void testQueryDSL() {
        // given
        // QueryDSL에서 사용할 Q클래스 객체를 받아온다
        // (클래스 내에 상수로 객체가 선언되어 있기에 직접 생성할 필요가 없다.)
        QMember m = member;

        // when
        Member foundMember = factory.select(m)
                .from(m)
                .where(m.userName.eq("member1"))
                .fetchOne();

        // then
        assertEquals("member1", foundMember.getUserName());
    }
    
    
    @Test
    @DisplayName("search")
    void search() {
        // given
        String searchName = "member2";
        int searchAge = 20;
        
        QMember m = member;
        
        // when
        Member foundMember = factory.selectFrom(m) // SELECT * FROM 과 동일
//                .where(m.userName.eq(searchName), m.age.eq(searchAge)) // 파라미터를 콤마로 이어서 작성하면 and와 같음
                .where(m.userName.eq(searchName)
                        .and(m.age.eq(searchAge))) // .and 메서드를 사용하여 chaining해도 됨
                .fetchOne();

        // then
        assertEquals("teamA", foundMember.getTeam().getName());

        /*
         JPAQueryFactory를 이용해서 쿼리문을 조립한 후 반환 인자를 결정합니다.
         - fetchOne(): 단일 건 조회. 여러 건 조회시 예외 발생.
         - fetchFirst(): 단일 건 조회. 여러 개가 조회돼도 첫 번째 값만 반환
         - fetch(): List 형태로 반환

         * JPQL이 제공하는 모든 검색 조건을 queryDsl에서도 사용 가능
         *
         * member.userName.eq("member1") // userName = 'member1'
         * member.userName.ne("member1") // userName != 'member1'
         * member.userName.eq("member1").not() // userName != 'member1'
         * member.userName.isNotNull() // 이름이 is not null
         * member.age.in(10, 20) // age in (10,20)
         * member.age.notIn(10, 20) // age not in (10,20)
         * member.age.between(10, 30) // age between 10, 30
         * member.age.goe(30) // age >= 30
         * member.age.gt(30) // age > 30
         * member.age.loe(30) // age <= 30
         * member.age.lt(30) // age < 30
         * member.userName.like("_김%") // userName LIKE '_김%'
         * member.userName.contains("김") // userName LIKE '%김%'
         * member.userName.startsWith("김") // userName LIKE '김%'
         * member.userName.endsWith("김") // userName LIKE '%김'
         */
    }

    @Test
    @DisplayName("여러 결과 반환하기")
    void testFetchResult() {
        // fetch
        List<Member> fetch1 = factory.selectFrom(member).fetch();

        System.out.println("\n============fetch1==============");
        fetch1.forEach(System.out::println);

        // fetchOne
        Member fetch2 = factory.selectFrom(member)
                .where(member.id.eq(3L))
                .fetchOne();
        System.out.println("\n============fetch2==============");
        System.out.println("fetch2 = " + fetch2);

        // fetchFirst
        Member fetch3 = factory.selectFrom(member).fetchFirst();
        System.out.println("\n============fetch3==============");
        System.out.println("fetch3 = " + fetch3);
    }
}

