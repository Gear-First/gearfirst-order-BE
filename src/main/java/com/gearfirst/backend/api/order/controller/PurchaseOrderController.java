package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
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

    @Operation(summary = "엔지니어가 차량 리스트 조회", description = "대리점에서 엔지니어가 접수한 차량 리스트를 조회합니다.")
    @GetMapping("/receipts/vehicles/{engineerId}")
    public ResponseEntity<ApiResponse<List<ReceiptCarResponse>>> findReceiptsByEngineer(
            @PathVariable Long engineerId
    ) {
        List<ReceiptCarResponse> list = purchaseOrderService.findReceiptsByEngineer(engineerId);
        return ApiResponse.success(SuccessStatus.SEARCH_VEHICLE_SUCCESS, list);
    }

    @Operation(summary = "엔지니어가 차량 검색", description = "대리점에서 엔지니어가 부품 발주를 위해 차량번호로 검색합니다.")
    @GetMapping("/vehicles/search/{engineerId}")
    public ResponseEntity<ApiResponse<List<ReceiptCarResponse>>> searchReceiptsByEngineer(
            @PathVariable Long engineerId,
            @RequestParam(required = false) String keyword
    ) {
        List<ReceiptCarResponse> list = purchaseOrderService.searchReceiptsByEngineer(engineerId, keyword);
        return ApiResponse.success(SuccessStatus.SEARCH_VEHICLE_SUCCESS, list);
    }

    @Operation(summary = "차량에 맞는 부품 검색", description = "대리점에서 엔지니어가 부품을 검색합니다.")
    @GetMapping("/inventories")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoriesByCarModel(
            @RequestParam Long carModelId,
            @RequestParam(required = false) String keyword
    ) {
        List<InventoryResponse> list = purchaseOrderService.findInventoriesByCarModel(carModelId, keyword);
        return ApiResponse.success(SuccessStatus.SEARCH_INVENTORY_SUCCESS, list);
    }

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
