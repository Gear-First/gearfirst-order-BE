package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.PurchaseOrderResponse;

import java.util.List;

public interface PurchaseOrderService {
    //대리점 발주 요청 생성
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    //본사용 전체 조회(모든 대리점)
    List<PurchaseOrderResponse> getAllPurchaseOrders();
    //엔지니어용 발주 목록 전체 조회
    List<PurchaseOrderResponse> getBranchPurchaseOrders(Long branchId, Long engineerId);
    //대리점 상태 그룹별 조회(준비/ 완료 / 취소)
    List<PurchaseOrderResponse> getBranchPurchaseOrdersByFilter(Long branchId, String filterType);
    //본사 상태별 조회
    List<PurchaseOrderResponse> getHeadPurchaseOrdersByStatus(String status);
    //발주 상세 조회
    PurchaseOrderResponse getPurchaseOrderDetail(Long orderId);
    //발주 승인
    void approveOrder(Long orderId, Long warehouseId);
    //발주 반려
    void rejectOrder(Long orderId);
}
