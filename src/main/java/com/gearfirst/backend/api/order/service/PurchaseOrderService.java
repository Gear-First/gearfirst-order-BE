package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.RepairPartResponse;
import com.gearfirst.backend.api.order.infra.client.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.client.dto.ReceiptCarResponse;

import java.util.List;

public interface PurchaseOrderService {
    //대리점 발주 요청 생성
    void createPurchaseOrder(PurchaseOrderRequest request);

    /**
     * TODO:본사용 전체 조회(모든 대리점)
     */
    //List<PurchaseOrderResponse> getAllPurchaseOrders();
    //엔지니어용 발주 목록 전체 조회
    List<PurchaseOrderResponse> getBranchPurchaseOrders(Long branchId, Long engineerId);
    //대리점 상태 그룹별 조회(준비/ 완료 / 취소)
    List<PurchaseOrderResponse> getBranchPurchaseOrdersByFilter(Long branchId, Long engineerId, String filterType);
    /**
     * TODO: 본사 상태별 조회
     */
    //List<PurchaseOrderResponse> getHeadPurchaseOrdersByStatus(String status);
    //수리 완료 처리 및 발주 부품 조회
    List<RepairPartResponse> completeRepairAndGetParts(Long receiptId, String vehicleNumber, Long branchId, Long engineerId);
    //발주 상세 조회
    PurchaseOrderResponse getPurchaseOrderDetail(Long orderId,Long branchId, Long engineerId);
    //대리점 발주 취소
    void cancelBranchOrder(Long orderId, Long branchId, Long engineerId);
    //발주 승인
    void approveOrder(Long orderId, Long warehouseId);
    //발주 반려
    void rejectOrder(Long orderId);
}
