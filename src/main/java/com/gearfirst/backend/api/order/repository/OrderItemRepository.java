package com.gearfirst.backend.api.order.repository;

import com.gearfirst.backend.api.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    //특정 발주서의 세무 품목 목록 조회
    List<OrderItem> findByPurchaseOrder_Id(Long orderId);

    @Query("""
    SELECT oi 
    FROM OrderItem oi
    JOIN FETCH oi.purchaseOrder o
    JOIN FETCH o.branch b
    WHERE b.id = :branchId
""")
    List<OrderItem> findAllItemsByBranchId(@Param("branchId") Long branchId);
    // 특정 주문 ID 리스트에 해당하는 품목들 조회
    List<OrderItem> findByIdIn(List<Long> orderIds);

}


