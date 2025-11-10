package com.gearfirst.backend.api.order.service;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.common.context.UserContext;
import com.gearfirst.backend.common.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PurchaseOrderService {
    //대리점&창고 발주 요청 생성
    PurchaseOrderResponse createPurchaseOrder(UserContext user, PurchaseOrderRequest request);
    PageResponse<HeadPurchaseOrderResponse> getPendingOrders(
            UserContext user,
            LocalDate startDate, LocalDate endDate,
            String searchKeyword,
            Pageable pageable
    );

    PageResponse<HeadPurchaseOrderResponse> getProcessedOrders(
            UserContext user,
            LocalDate startDate, LocalDate endDate,
            String searchKeyword,
            String status,
            Pageable pageable
    );
    PageResponse<HeadPurchaseOrderResponse> getCancelOrders(
            UserContext user,
            LocalDate startDate, LocalDate endDate,
            String searchKeyword,
            String status,
            Pageable pageable
    );
     //본사용 발주 상세 조회
     HeadPurchaseOrderDetailResponse getHeadPurchaseOrderDetail(UserContext user, Long orderId);

     //대리점 창고 발주 목록 전체 조회
     PageResponse<PurchaseOrderDetailResponse> getBranchPurchaseOrders(UserContext user, LocalDate startDate, LocalDate endDate, Pageable pageable);
    //대리점 상태 그룹별 조회(준비/ 완료 / 취소)
    PageResponse<PurchaseOrderDetailResponse> getBranchPurchaseOrdersByFilter(
            UserContext user, String filterType,
            LocalDate startDate, LocalDate endDate, Pageable pageable
    );

    //발주 부품 조회
    PurchaseOrderResponse getCompleteRepairPartsList(UserContext user, String receiptNum, String vehicleNumber);

    //발주 상세 조회
    PurchaseOrderDetailResponse getPurchaseOrderDetail(UserContext user, Long orderId);
    //대리점&창고 발주 취소
    void cancelBranchOrder(UserContext user, Long orderId);
    //발주 승인
    void approveOrder(UserContext user, Long orderId,String note);
    //출고날짜 업데이트
    void ship(UserContext user, Long orderId);
    //발주 반려
    void rejectOrder(UserContext user, Long orderId, String note);
}
