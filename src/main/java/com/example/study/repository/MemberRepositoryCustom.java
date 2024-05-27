package com.example.study.repository;

import com.example.study.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// QueryDSL용 추상 메서드
public interface MemberRepositoryCustom {
    List<Member> findByName(String name); // JPA 쿼리 메서드 아니다!! 상속 안 받았다!
}
