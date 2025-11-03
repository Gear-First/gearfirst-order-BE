package com.gearfirst.backend.api.order.repository;

import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.api.order.entity.QOrderItem;
import com.gearfirst.backend.api.order.entity.QPurchaseOrder;
import com.gearfirst.backend.common.enums.OrderStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

//QueryDSL 전용 커스텀 Repository
@Repository
@RequiredArgsConstructor
public class PurchaseOrderQueryRepository {
    private final JPAQueryFactory query;

    public Page<PurchaseOrder> searchByStatus(
            LocalDate startDate, LocalDate endDate,
            String branchCode, String partName,
            OrderStatus status,
            Pageable pageable
    ) {
        QPurchaseOrder purchaseOrder = QPurchaseOrder.purchaseOrder;
        QOrderItem orderItem = QOrderItem.orderItem;
        //동적 조건(필터)을 누적할 때 쓰는 가변형 조건 컨테이너
        //내부적으로 논리식을 담고 있고 and(), or() 등으로 조건을 누적할 수 있음
        BooleanBuilder where = new BooleanBuilder();

        if (status != OrderStatus.PENDING) {
            // 나머지 상태 전체
            where.and(purchaseOrder.status.ne(OrderStatus.PENDING));
        } else {
            // PENDING만
            where.and(purchaseOrder.status.eq(OrderStatus.PENDING));
        }

        if(hasText(partName)) where.and(orderItem.partName.containsIgnoreCase(partName));

        if(startDate != null) where.and(purchaseOrder.requestDate.goe(startDate.atStartOfDay())); //requestDate >= startDate
        if(endDate != null) where.and(purchaseOrder.requestDate.loe(endDate.atTime(23,59,59))); //requestDate <= endDate 23:59:59
        if(hasText(branchCode)) where.and(purchaseOrder.branchCode.containsIgnoreCase(branchCode)); //부분일치, 대소문자 무시 검색

        List<PurchaseOrder> content = query
                .selectDistinct(purchaseOrder) //1:N 조인 시 중복 제거
                .from(orderItem)        //기준 테이블
                .join(orderItem.purchaseOrder, purchaseOrder)
                .where(where)
                .orderBy(toOrderSpecifiers(pageable.getSort(), purchaseOrder))
                .offset(pageable.getOffset())  //pageable의 정렬을 QueryDSL OrderSpecifier로 변환
                .limit(pageable.getPageSize())
                .fetch();
        //총 개수 조회
        Long total = query
                .select(purchaseOrder.countDistinct()) //중복 제거 카운트
                .from(orderItem)
                .join(orderItem.purchaseOrder, purchaseOrder)
                .where(where)
                .fetchOne();

        //PageImpl로 스프링 페이지 결과 생성
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort, QPurchaseOrder purchaseOrder) {
        // Q타입의 변수명(메타데이터)로 PathBuilder 생성
        PathBuilder<?> entityPath = new PathBuilder<>(purchaseOrder.getType(), purchaseOrder.getMetadata());

        // sort 비어 있을 경우 기본 정렬 지정 (예: id 내림차순)
        if (sort.isEmpty()) {
            return new OrderSpecifier[]{ new OrderSpecifier<>(Order.DESC, purchaseOrder.id) };
        }
        return sort.stream()
                .filter(order -> order.getProperty() != null && !order.getProperty().isBlank()) //빈 문자열 필터링
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    ComparableExpression<?> sortExpr =
                            entityPath.getComparable(order.getProperty(), Comparable.class);
                    return new OrderSpecifier<>(direction, sortExpr);
                })
                .toArray(OrderSpecifier[]::new);
    }

}
