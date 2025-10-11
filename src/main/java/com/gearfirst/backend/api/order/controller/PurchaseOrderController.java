package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.service.PurchaseOrderService;
import com.gearfirst.backend.common.response.ApiResponse;
import com.gearfirst.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@AllArgsConstructor
@Tag(name = "Purchase Order API", description = "대리점 발주 요청/조회 API")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "발주 요청 생성", description = "대리점이 ERP로 발주 요청을 보냅니다.")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> requestPurchaseOrder(
            @RequestBody PurchaseOrderRequest request
    ){
        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
        return ApiResponse.success(SuccessStatus.REQUEST_PURCHASE_SUCCESS, response);
    }

    @Operation(summary = "발주 전체 조회", description = "대리점이 등록한 발주 내역을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getPurchaseOrders(
            @RequestParam Long branchId
    ){
        List<PurchaseOrderResponse> list = purchaseOrderService.getAllPurchaseOrders(branchId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    @Operation(summary = "발주 상태별 조회", description = "지정된 상태의 발주 내역만 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getPurchaseOrders(
            @RequestParam Long branchId, @RequestParam(required = false) String status
    ){
        List<PurchaseOrderResponse> list = purchaseOrderService.getPurchaseOrdersByStatus(branchId, status);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    @Operation(summary = "발주 상세 조회", description = "특정 발주 번호 상세 조회")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrderDetail(@PathVariable Long orderId){
        PurchaseOrderResponse detail = purchaseOrderService.getPurchaseOrderDetail(orderId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,detail);
    }

    @Operation(summary = "발주 승인", description = "본사에서 발주를 승인합니다.")
    @PatchMapping("/{orderId}/{warehouseId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long orderId, @PathVariable Long warehouseId){
        purchaseOrderService.approveOrder(orderId, warehouseId);
        return ApiResponse.success_only(SuccessStatus.APPROVE_PURCHASE_SUCCESS);
    }
    @Operation(summary = "발주 반려", description = "본사에서 발주를 반려합니다.")
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long orderId){
        purchaseOrderService.rejectOrder(orderId);
        return ApiResponse.success_only(SuccessStatus.REJECT_PURCHASE_SUCCESS);
    }
}
