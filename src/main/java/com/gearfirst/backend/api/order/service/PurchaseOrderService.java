package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.infra.client.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.client.dto.ReceiptCarResponse;

import java.util.List;

public interface PurchaseOrderService {
    //엔지니어가 접수한 차량 리스트
    List<ReceiptCarResponse> findReceiptsByEngineer(Long engineerId);
    //키워드로 차량 검색
    List<ReceiptCarResponse> searchReceiptsByEngineer(Long engineerId, String keyword);
    //차량에 맞는 부품 검색
    List<InventoryResponse> findInventoriesByCarModel(Long CarModelId, String keyword);
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
    //발주 상세 조회
    PurchaseOrderResponse getPurchaseOrderDetail(Long orderId,Long branchId, Long engineerId);
    //대리점 발주 취소
    void cancelBranchOrder(Long orderId, Long branchId, Long engineerId);
    //발주 승인
    void approveOrder(Long orderId, Long warehouseId);
    //발주 반려
    void rejectOrder(Long orderId);
}
