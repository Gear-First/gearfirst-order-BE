package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.PurchaseOrderResponse;

import java.util.List;

public interface PurchaseOrderService {
    //발주 요청 생성
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    //대리점 발주 목록 조회
    List<PurchaseOrderResponse> getAllPurchaseOrders(Long branchId);
    //발주 상태로 필터링 조회
    List<PurchaseOrderResponse> getPurchaseOrdersByStatus(Long branchId, String status);
    //발주 상세 조회
    PurchaseOrderResponse getPurchaseOrderDetail(Long orderId);
    //발주 승인
    void approveOrder(Long orderId, Long warehouseId);
    //발주 반려
    void rejectOrder(Long orderId);
}
