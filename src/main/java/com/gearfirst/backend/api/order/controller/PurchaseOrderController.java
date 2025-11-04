package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.request.NoteRequest;
import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderDetailResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@AllArgsConstructor
@Tag(name = "Purchase Order API", description = "발주 요청/조회 API")
public class PurchaseOrderController {


    private final PurchaseOrderService purchaseOrderService;
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName,
            @RequestHeader("X-User-Rank") String rank,
            @RequestHeader("X-User-Region") String region,
            @RequestHeader("X-User-WorkType") String workType
    ) {
            Map<String, String> result = new HashMap<>();
            result.put("userId", userId);
            result.put("username", userName);
            result.put("rank", rank);
            result.put("region", region);
            result.put("workType", workType);
            return ResponseEntity.ok(result);
    }

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
    @Operation(summary = "본사 발주 처리건 전체 조회", description = "대리점이 요청한 발주 내역을 본사에서 승인/반려 후 전체조회 또는 날짜/대리점이름/부품이름으로 필터링하여  조회합니다.")
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

    @Operation(summary = "본사 발주 상세 조회", description = "발주 상세내역을 조회합니다.")
    @GetMapping("/head/{orderId}")
    public ResponseEntity<ApiResponse<HeadPurchaseOrderDetailResponse>> getPurchaseOrderDetail(
            @PathVariable Long orderId
    ){
        HeadPurchaseOrderDetailResponse response= purchaseOrderService.getPurchaseOrderDetail(orderId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,response);
    }

    @Operation(summary = "대리점 발주 전체 조회", description = "엔지니어가 자신이 등록한 발주 내역을 조회합니다.")
    @GetMapping("/branch")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDetailResponse>>> getBranchPurchaseOrders(
            @RequestParam String branchCode, @RequestParam Long engineerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable

    ){
        PageResponse<PurchaseOrderDetailResponse> list = purchaseOrderService.getBranchPurchaseOrders(branchCode,engineerId,startDate,endDate,pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    @Operation(summary = "대리점 발주 상태 그룹별 조회", description = "준비 / 완료 / 취소·반려 그룹별로 발주 내역을 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDetailResponse>>> getBranchPurchaseOrdersByFilter(
            @RequestParam String branchCode, @RequestParam Long engineerId,  @RequestParam String filterType,  // "ready", "completed", "cancelled"
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ){
        PageResponse<PurchaseOrderDetailResponse> list = purchaseOrderService.getBranchPurchaseOrdersByFilter(branchCode, engineerId, filterType,startDate,endDate,pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

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


    @Operation(summary = "발주 승인", description = "본사에서 발주를 승인합니다.")
    @PatchMapping("/{orderId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long orderId, @RequestBody NoteRequest request){
        purchaseOrderService.approveOrder(orderId, request.getNote());
        return ApiResponse.success_only(SuccessStatus.APPROVE_PURCHASE_SUCCESS);
    }
    @Operation(summary = "발주 반려", description = "본사에서 발주를 반려합니다.")
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long orderId, @RequestBody NoteRequest request){
        purchaseOrderService.rejectOrder(orderId, request.getNote());
        return ApiResponse.success_only(SuccessStatus.REJECT_PURCHASE_SUCCESS);
    }

}
