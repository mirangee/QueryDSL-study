package com.example.study.repository;

import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.example.study.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
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
import static com.example.study.entity.QTeam.team;
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

//    @Test
    void testInsertData() {

//        Team teamA = Team.builder()
//                .name("teamA")
//                .build();
//        Team teamB = Team.builder()
//                .name("teamB")
//                .build();
//
//        teamRepository.save(teamA);
//        teamRepository.save(teamB);

        Team teamA = teamRepository.findById(1L).orElseThrow();
        Team teamB = teamRepository.findById(2L).orElseThrow();

        Member member1 = Member.builder()
                .userName("member9")
                .age(10)
                .team(teamA)
                .build();
        Member member2 = Member.builder()
                .userName("member10")
                .age(20)
                .team(teamA)
                .build();
        Member member3 = Member.builder()
                .userName("member11")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = Member.builder()
                .userName("member12")
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

    @Test
    @DisplayName("QueryDSL custom 설정 확인")
    void queryDslCustom() {
        // given
        String name = "member4";
        // when
        List<Member> result = memberRepository.findByName(name);
        // then
        assertEquals(1, result.size());
        assertEquals("teamB", result.get(0).getTeam().getName());
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // QueryDSL 실습
    // SQL 실행 순서
    // FROM -> (JOIN ON) -> WHERE -> GROUP BY -> HAVING -> SELECT -> ORDER BY
    @Test
    @DisplayName("회원 정렬 조회")
    void sort() {
        // given

        // when
        List<Member> result = factory.selectFrom(member)
//                .where(원하는 조건)
                .orderBy(member.age.desc())
                .fetch();
        // then
        assertEquals(12,result.size());

        result.forEach(System.out::println);
    }
    
    @Test
    @DisplayName("QueryDSL paging")
    void paging() {
        // given
        
        // when
        List<Member> result = factory.selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(3)
                .limit(3) // MySQL에서 LIMIT(3,3)이라고 쓴 것과 동일
                .fetch();
        // then
        assertEquals(3,result.size());
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("그룹 함수의 종류")
    void aggregation() {
        // given

        // when
        List<Tuple> result = factory.select(
                        member.count(),
                        member.age.sum(), // SUM(age)
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                        )
                        .from(member)
                        .fetch();
        
        // Tuple : QueryDSL에서 쿼리로 나온 결과 행들을 타입에 맞춰 담을 수 있게 제공되는 타입
        Tuple tuple = result.get(0);

        // then
        assertEquals(12, tuple.get(member.count()));
        assertEquals(40, tuple.get(member.age.max()));
        assertEquals(10, tuple.get(member.age.min()));

        System.out.println("\n\n\n");
        System.out.println("result = " + result);
        System.out.println("tuple = " + tuple);
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("Group By, Having")
    void testGroupBy() {
        // given
        List<Tuple> result = factory.select(member.age, member.age.count())
                .from(member)
                .groupBy(member.age)
                .having(member.age.count().goe(2))
                .orderBy(member.age.asc())
                .fetch();
        // when

        // then
        result.forEach(System.out::println);
    }
    
    @Test
    @DisplayName("join 해보기")
    void join() {
        // given
        
        // when
        List<Tuple> result = factory.select(member.userName, team.name).from(member)
                // .join(기준Entity.조인대상Entity, 별칭(Q클래스))
                .join(member.team, team) // inner join으로 진행됨
                .where(team.name.eq("teamA"))
                .fetch();

        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }


    /*
    ex) 회원과 팀을 join하면서, 팀 이름이 teamA인 팀만 조회, 회원은 모두 조회되어야 한다.
     SQL : SELECT m.*, t.* FROM tbl_member m LEFT OUTER JOIN tbl_team t ON m.team_id = t.team_id WHERE t.name = 'teamA';
     JPQL : SELECT m, t FROM Member m LEFT JOIN m.team t ON t.name = "teamA" (이미 연관관계 매핑을 해놨기 때문에 ON에 일반 조건만 붙이면 됨)
    */
    @Test
    @DisplayName("left outer join")
    void leftJoinTest() {
        // given
        List<Tuple> result = factory.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();
        // when

        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("sub query 사용하기 (나이가 가장 많은 회원을 조회")
    void subQueryTest() {
        // given
        // 같은 테이블에서 서브쿼리를 적용하려면 별도의 QCLASS 객체를 생성해야 한다.
        QMember memberSub = new QMember("memberSub");

        // when
        List<Member> result = factory.selectFrom(member)
                .where(member.age.gt(
                        // 나이가 가장 많은 사람을 조회하는 서브쿼리문이 들어가야 함
                        JPAExpressions // 서브쿼리를 사용할 수 있게 도와주는 클래스(WHERE절, SELECT절 가능 / FROM절(인라인뷰)은 불가
                                .select(memberSub.age.avg())
                                .from(memberSub)
                        // (SELECT MAX(age) FROM tbl_member)
                ))
                .fetch();

        // then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }
}

