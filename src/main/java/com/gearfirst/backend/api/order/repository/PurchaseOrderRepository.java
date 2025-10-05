package com.gearfirst.backend.api.order.repository;

import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import com.gearfirst.backend.common.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long> {
    //대리점 별 발주 내역 조회
    List<PurchaseOrder> findByBranch_Id(Long branchId);
    //상태별 주문 처리 목록 확인
    List<PurchaseOrder> findByStatus(OrderStatus status);
}
