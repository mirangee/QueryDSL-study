package com.example.study.repository;

import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.study.entity.QMember.*;

// QueryDSL용 인터페이스의 구현체는 반드시 '-Impl'로 끝나야 자동으로 인식되어서
// 원본 인터페이스 타입(MemberRepository)의 객체로도 사용이 가능하다.
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Member> findByName(String name) {
        return queryFactory.selectFrom(member)
                .where(member.userName.eq(name))
                .fetch();
    }

    // 동적 SQL 작성하는 방법
    @Override
    public List<Member> findUser(String nameParam, Integer ageParam) {
       return queryFactory
               .selectFrom(member)
               .where(nameEq(nameParam), ageEq(ageParam))
               .fetch();
    }

    // WHERE 절에 BooleanExpression을 리턴하는 메서드를 직접 작성한다.
    // nameEq는 전달받은 값이 없다면 null을 리턴하고, 그렇지 않을 경우 논리 표현식 결과를 리턴한다.
    // WHERE절에서는 null 값인 경우 조건을 건너 뛴다(쿼리를 완성하지 않음)

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    private BooleanExpression nameEq(String nameParam) {
        if (nameParam != null && nameParam.isEmpty()) {
            return member.userName.eq(nameParam);
        }
        return null;
    }
}
