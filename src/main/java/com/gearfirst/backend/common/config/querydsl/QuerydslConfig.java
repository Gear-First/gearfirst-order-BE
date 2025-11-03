package com.gearfirst.backend.common.config.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {
    @PersistenceContext
    private EntityManager em; //JPA의 EntityManager를 주입받음

    /**
     * QueryDSL의 핵심 객체
     * 이걸 Repository에서 주입받아 .select(), .where() 등을 사용할 수 있게 됨
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
