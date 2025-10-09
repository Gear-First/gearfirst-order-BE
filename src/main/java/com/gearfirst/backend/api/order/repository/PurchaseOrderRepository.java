package com.gearfirst.backend.api.order.repository;

import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    //대리점 별 발주 내역 조회
    List<PurchaseOrder> findByBranch_Id(Long branchId);
    //상태별 주문 처리 목록 확인
    List<PurchaseOrder> findByStatus(OrderStatus status);
    // 상태(status) 기준으로 발주 목록 조회
    @Query("""
        SELECT o 
        FROM PurchaseOrder o 
        WHERE o.branch.id = :branchId
        AND (:status IS NULL OR o.status = :status)
    """)
    List<PurchaseOrder> findByBranchAndStatus(
            @Param("branchId") Long branchId,
            @Param("status") OrderStatus status
    );
}
