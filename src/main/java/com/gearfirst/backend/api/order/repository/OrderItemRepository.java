package com.gearfirst.backend.api.order.repository;

import com.gearfirst.backend.api.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    //특정 발주서의 세무 품목 목록 조회
    List<OrderItem> findByPurchaseOrder_Id(Long orderId);
}
