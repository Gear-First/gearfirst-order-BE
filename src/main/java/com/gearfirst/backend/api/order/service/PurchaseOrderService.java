package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseOrderService {
    //대리점 발주 요청 생성
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);

    //본사용 발주 전체 조회(모든 대리점)-승인 대기 상태
    PageResponse<HeadPurchaseOrderResponse> getPendingOrders(
            LocalDate startDate, LocalDate endDate,
            String branchCode, String partName,
            Pageable pageable
    );
     //본사용 전체 조회(모든 대리점)-나머지 상태
     PageResponse<HeadPurchaseOrderResponse> getOtherOrders(
             LocalDate startDate, LocalDate endDate,
             String branchCode, String partName,
             Pageable pageable
     );

    //엔지니어용 발주 목록 전체 조회
    List<PurchaseOrderDetailResponse> getBranchPurchaseOrders(String branchCode, Long engineerId);
    //대리점 상태 그룹별 조회(준비/ 완료 / 취소)
    List<PurchaseOrderDetailResponse> getBranchPurchaseOrdersByFilter(String branchCode, Long engineerId, String filterType);
    /**
     * TODO: 본사 상태별 조회
     */
    //List<PurchaseOrderResponse> getHeadPurchaseOrdersByStatus(String status);
    //발주 부품 조회
    PurchaseOrderResponse getCompleteRepairPartsList(String receiptNum, String vehicleNumber, String branchCode, Long engineerId);
    //수리 완료 처리
    PurchaseOrderResponse completeRepairPartsList(String receiptNum, String vehicleNumber, String branchCode, Long engineerId);
    //발주 상세 조회
    PurchaseOrderDetailResponse getPurchaseOrderDetail(Long orderId, String branchCode, Long engineerId);

    //대리점 발주 취소
    void cancelBranchOrder(Long orderId, String branchCode, Long engineerId);
    //발주 승인
    void approveOrder(Long orderId, Long warehouseId);
    //발주 반려
    void rejectOrder(Long orderId);
}
