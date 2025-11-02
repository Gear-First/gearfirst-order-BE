package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.service.PurchaseOrderService;
import com.gearfirst.backend.common.dto.response.PageResponse;
import com.gearfirst.backend.common.response.ApiResponse;
import com.gearfirst.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@AllArgsConstructor
@Tag(name = "Purchase Order API", description = "발주 요청/조회 API")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;


    @Operation(summary = "발주 요청 생성", description = "대리점이 본사로 발주 요청을 보냅니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> requestPurchaseOrder(
            @RequestBody PurchaseOrderRequest request
    ){
        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
        return ApiResponse.success(SuccessStatus.REQUEST_PURCHASE_SUCCESS, response);
    }

    @Operation(summary = "본사 발주 요청 건 전체 조회", description = "대리점이 요청한 발주 내역을 전체조회 또는 날짜/대리점이름/부품이름으로 필터링하여  조회합니다.")
    @GetMapping("/head/orders/pending")
    public ResponseEntity<ApiResponse<PageResponse<HeadPurchaseOrderResponse>>> getPendingOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String partName,

            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        PageResponse<HeadPurchaseOrderResponse> page =
        purchaseOrderService.getPendingOrders(startDate, endDate, branchCode, partName, pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,page);
    }
    @Operation(summary = "본사 발주 전체 조회", description = "대리점이 요청한 발주 내역을 본사에서 승인/반려 후 전체조회 또는 날짜/대리점이름/부품이름으로 필터링하여  조회합니다.")
    @GetMapping("/head/orders/other")
    public ResponseEntity<ApiResponse<PageResponse<HeadPurchaseOrderResponse>>> getOtherOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String partName,

            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        PageResponse<HeadPurchaseOrderResponse> page =
                purchaseOrderService.getOtherOrders(startDate, endDate, branchCode, partName, pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,page);
    }

    @Operation(summary = "대리점 발주 전체 조회", description = "엔지니어가 자신이 등록한 발주 내역을 조회합니다.")
    @GetMapping("/branch")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDetailResponse>>> getBranchPurchaseOrders(
            @RequestParam String branchCode, @RequestParam Long engineerId
    ){
        List<PurchaseOrderDetailResponse> list = purchaseOrderService.getBranchPurchaseOrders(branchCode,engineerId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    @Operation(summary = "대리점 발주 상태 그룹별 조회", description = "준비 / 완료 / 취소·반려 그룹별로 발주 내역을 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<PurchaseOrderDetailResponse>>> getBranchPurchaseOrdersByFilter(
            @RequestParam String branchCode, @RequestParam Long engineerId,  @RequestParam String filterType  // "ready", "completed", "cancelled"
    ){
        List<PurchaseOrderDetailResponse> list = purchaseOrderService.getBranchPurchaseOrdersByFilter(branchCode, engineerId, filterType);
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
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> getPurchaseOrderDetail(
            @PathVariable Long orderId, @RequestParam String branchCode, @RequestParam Long engineerId
    ){
        PurchaseOrderDetailResponse detail = purchaseOrderService.getPurchaseOrderDetail(orderId,branchCode, engineerId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,detail);
    }

    @Operation(summary = "대리점에서 발주 취소", description = "대리점에서 승인 대기, 승인 완료의 상태 발주만 취소합니다.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBranchOrder(@PathVariable Long orderId, @RequestParam String branchCode, @RequestParam Long engineerId){
        purchaseOrderService.cancelBranchOrder(orderId,branchCode,engineerId);
        return ApiResponse.success_only(SuccessStatus.CANCEL_PURCHASE_SUCCESS);
    }

    @Operation(summary = "대리점에서 수리 완료 시 발주한 부품 목록 조회", description = "대리점에서 수리 완료 버튼 클릭 시 해당 발주의 부품 목록을 반환합니다.")
    @GetMapping("/repair/parts/{receiptNum}/{vehicleNumber}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getCompleteRepairParts(@PathVariable String receiptNum, @PathVariable String vehicleNumber, @RequestParam String branchCode, @RequestParam Long engineerId ){
        PurchaseOrderResponse response = purchaseOrderService.getCompleteRepairPartsList(receiptNum,vehicleNumber,branchCode,engineerId);
        return ApiResponse.success(SuccessStatus.SEARCH_PARTS_SUCCESS,response);
    }

    @Operation(summary = "대리점에서 발주 상태 수리 완료 변경 후 부품 반환", description = "대리점에서 발주 상태를 '수리에 사용됨'으로 변경하고, 해당 발주의 부품 목록을 반환합니다.")
    @PostMapping("/complete/parts/{receiptNum}/{vehicleNumber}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> completeRepairParts(@PathVariable String receiptNum, @PathVariable String vehicleNumber, @RequestParam String branchCode, @RequestParam Long engineerId ){
        PurchaseOrderResponse response = purchaseOrderService.completeRepairPartsList(receiptNum,vehicleNumber,branchCode,engineerId);
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
