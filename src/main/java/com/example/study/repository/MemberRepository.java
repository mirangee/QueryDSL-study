package com.example.study.repository;

import com.example.study.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // JPQL 사용 방법(Spring Data JPA 사용 시)
    @Query("SELECT m FROM Member m WHERE m.userName = :userName")
    Optional<Member> findMember(@Param("userName") String userName);

    // JPQL 사용 방법(생 JPA 사용 시) -> test 클래스 참고
}
