package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.RepairPartResponse;
import com.gearfirst.backend.api.order.infra.client.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.client.dto.ReceiptCarResponse;
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
@Tag(name = "Purchase Order API", description = "발주 요청/조회 API")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;


    @Operation(summary = "발주 요청 생성", description = "대리점이 본사로 발주 요청을 보냅니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> requestPurchaseOrder(
            @RequestBody PurchaseOrderRequest request
    ){
        purchaseOrderService.createPurchaseOrder(request);
        return ApiResponse.success_only(SuccessStatus.REQUEST_PURCHASE_SUCCESS);
    }

    /**
     * TODO:본사용 전체 조회(모든 대리점)
     */
//    @Operation(summary = "본사 발주 전체 조회", description = "대리점이 등록한 발주 내역을 조회합니다.")
//    @GetMapping("/head")
//    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getAllPurchaseOrders(){
//        List<PurchaseOrderResponse> list = purchaseOrderService.getAllPurchaseOrders();
//        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
//    }

    @Operation(summary = "대리점 발주 전체 조회", description = "엔지니어가 자신이 등록한 발주 내역을 조회합니다.")
    @GetMapping("/branch")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getBranchPurchaseOrders(
            @RequestParam Long branchId, @RequestParam Long engineerId
    ){
        List<PurchaseOrderResponse> list = purchaseOrderService.getBranchPurchaseOrders(branchId,engineerId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    @Operation(summary = "대리점 발주 상태 그룹별 조회", description = "준비 / 완료 / 취소·반려 그룹별로 발주 내역을 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getBranchPurchaseOrdersByFilter(
            @RequestParam Long branchId, @RequestParam Long engineerId,  @RequestParam String filterType  // "ready", "completed", "cancelled"
    ){
        List<PurchaseOrderResponse> list = purchaseOrderService.getBranchPurchaseOrdersByFilter(branchId, engineerId, filterType);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    /**
     * TODO:본사 상태별 조회
     */
//    @Operation(summary = "본사 발주 상태별 조회", description = "승인대기, 승인완료, 반려, 출고중, 납품완료, 취소 상태별로 발주 내역을 조회합니다.")
//    @GetMapping("/head/status")
//    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getHeadPurchaseOrdersByStatus(
//            @RequestParam String status  // "PENDING", "APPROVED", "REJECTED", "SHIPPED", "COMPLETED", "CANCELLED"
//    ) {
//        List<PurchaseOrderResponse> list = purchaseOrderService.getHeadPurchaseOrdersByStatus(status);
//        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS, list);
//    }

    @Operation(summary = "대리점에서 발주 상세 조회", description = "특정 발주 번호 상세 조회")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrderDetail(
            @PathVariable Long orderId, @RequestParam Long branchId, @RequestParam Long engineerId
    ){
        PurchaseOrderResponse detail = purchaseOrderService.getPurchaseOrderDetail(orderId, branchId, engineerId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,detail);
    }

    @Operation(summary = "대리점에서 발주 취소", description = "대리점에서 승인 대기, 승인 완료의 상태 발주만 취소합니다.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBranchOrder(@PathVariable Long orderId, @RequestParam Long branchId, @RequestParam Long engineerId){
        purchaseOrderService.cancelBranchOrder(orderId,branchId,engineerId);
        return ApiResponse.success_only(SuccessStatus.CANCEL_PURCHASE_SUCCESS);
    }

    @Operation(summary = "대리점에서 수리 완료 시 발주한 부품 목록 조회", description = "대리점에서 발주 상태를 '수리에 사용됨'으로 변경하고, 해당 발주의 부품 목록을 반환합니다.")
    @PostMapping("/complete/parts{repairId}/{vehicleNumber}")
    public ResponseEntity<ApiResponse<List<RepairPartResponse>>> completeRepairParts(@PathVariable Long repairId, @PathVariable String vehicleNumber, @RequestParam Long branchId, @RequestParam Long engineerId ){
        List<RepairPartResponse> response = purchaseOrderService.completeRepairAndGetParts(repairId,vehicleNumber,branchId,engineerId);
        return ApiResponse.success(SuccessStatus.SEARCH_PARTS_SUCCESS,response);
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
