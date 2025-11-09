package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.request.NoteRequest;
import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.HeadPurchaseOrderResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderDetailResponse;
import com.gearfirst.backend.api.order.dto.response.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.service.PurchaseOrderService;
import com.gearfirst.backend.api.schedule.service.ScheduledTaskService;
import com.gearfirst.backend.common.annotation.CurrentUser;
import com.gearfirst.backend.common.context.UserContext;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@AllArgsConstructor
@Tag(name = "Purchase Order API", description = "발주 요청/조회 API")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final ScheduledTaskService scheduledTaskService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String encodedName,
            @RequestHeader("X-User-Rank") String encodedRank,
            @RequestHeader("X-User-Region") String encodedRegion,
            @RequestHeader("X-User-WorkType") String encodedWorkType
    ) {
        Map<String, String> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", new String(Base64.getDecoder().decode(encodedName), StandardCharsets.UTF_8));
        result.put("rank", new String(Base64.getDecoder().decode(encodedRank), StandardCharsets.UTF_8));
        result.put("region", new String(Base64.getDecoder().decode(encodedRegion), StandardCharsets.UTF_8));
        result.put("workType", new String(Base64.getDecoder().decode(encodedWorkType), StandardCharsets.UTF_8));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "발주 요청 생성", description = "대리점 또는 창고에서 본사로 발주 요청을 보냅니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> requestPurchaseOrder(
            @CurrentUser UserContext user, @RequestBody PurchaseOrderRequest request
    ){
        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(user, request);
        return ApiResponse.success(SuccessStatus.REQUEST_PURCHASE_SUCCESS, response);
    }

    @Operation(summary = "본사 발주 요청 건 전체 조회", description = "대리점이 요청한 발주 내역을 전체조회 또는 날짜/대리점이름/발부번호로 필터링하여  조회합니다.")
    @GetMapping("/head/orders/pending")
    public ResponseEntity<ApiResponse<PageResponse<HeadPurchaseOrderResponse>>> getPendingOrders(
            @CurrentUser UserContext user,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String search,

            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        PageResponse<HeadPurchaseOrderResponse> page =
        purchaseOrderService.getPendingOrders(user, startDate, endDate, search, pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,page);
    }
    @Operation(summary = "본사 발주 처리건 전체 조회", description = "대리점이 요청한 발주 내역을 본사에서 출고중,승인완료,납품완료 상태의 데이터를 전체조회 또는 날짜/대리점이름/발주번호/상태로 필터링하여  조회합니다.")
    @GetMapping("/head/orders/processed")
    public ResponseEntity<ApiResponse<PageResponse<HeadPurchaseOrderResponse>>> getProcessedOrders(
            @CurrentUser UserContext user,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,

            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        PageResponse<HeadPurchaseOrderResponse> page =
                purchaseOrderService.getProcessedOrders(user, startDate, endDate, search, status, pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,page);
    }

    @Operation(summary = "본사 발주 취소건 전체 조회", description = "대리점이 요청한 발주 내역을 본사에서 반려,취소 상태의 데이터를 전체조회 또는 날짜/대리점이름/발주번호/상태로 필터링하여  조회합니다.")
    @GetMapping("/head/orders/cancel")
    public ResponseEntity<ApiResponse<PageResponse<HeadPurchaseOrderResponse>>> getCanceledOrders(
            @CurrentUser UserContext user,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,

            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        PageResponse<HeadPurchaseOrderResponse> page =
                purchaseOrderService.getCancelOrders(user, startDate, endDate, search, status, pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,page);
    }

    @Operation(summary = "본사 발주 상세 조회", description = "발주 상세내역을 조회합니다.")
    @GetMapping("/head/{orderId}")
    public ResponseEntity<ApiResponse<HeadPurchaseOrderDetailResponse>> getHeadPurchaseOrderDetail(
            @CurrentUser UserContext user, @PathVariable Long orderId
    ){
        HeadPurchaseOrderDetailResponse response= purchaseOrderService.getHeadPurchaseOrderDetail(user, orderId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,response);
    }

    @Operation(summary = "대리점 발주 전체 조회", description = "엔지니어가 자신이 등록한 발주 내역을 전체조회, 날짜로 필터링 조회합니다.")
    @GetMapping("/branch")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDetailResponse>>> getBranchPurchaseOrders(
            @CurrentUser UserContext user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable

    ){
        PageResponse<PurchaseOrderDetailResponse> list = purchaseOrderService.getBranchPurchaseOrders(user,startDate,endDate,pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    @Operation(summary = "대리점 발주 상태 그룹별 조회", description = "준비 / 완료 / 취소·반려 그룹별로 발주 내역을 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDetailResponse>>> getBranchPurchaseOrdersByFilter(
            @CurrentUser UserContext user, @RequestParam String filterType,  // "ready", "completed", "cancelled"
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ){
        PageResponse<PurchaseOrderDetailResponse> list = purchaseOrderService.getBranchPurchaseOrdersByFilter(user, filterType,startDate,endDate,pageable);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
    }

    @Operation(summary = "대리점에서 발주 상세 조회", description = "특정 발주 번호 상세 조회")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> getPurchaseOrderDetail(
            @CurrentUser UserContext user,
            @PathVariable Long orderId
    ){
        PurchaseOrderDetailResponse detail = purchaseOrderService.getPurchaseOrderDetail(user, orderId);
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,detail);
    }

    @Operation(summary = "대리점에서 발주 취소", description = "대리점에서 승인 대기, 승인 완료의 상태 발주만 취소합니다.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBranchOrder(
            @CurrentUser UserContext user,
            @PathVariable Long orderId
    ){
        purchaseOrderService.cancelBranchOrder(user, orderId);
        return ApiResponse.success_only(SuccessStatus.CANCEL_PURCHASE_SUCCESS);
    }

    @Operation(summary = "수리 접수내역 조회 시 발주 부품 조회", description = "대리점에서 수리내역 조회 시 해당 발주의 부품 목록을 반환합니다.")
    @GetMapping("/repair/parts/{receiptNum}/{vehicleNumber}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getCompleteRepairParts(@CurrentUser UserContext user,@PathVariable String receiptNum, @PathVariable String vehicleNumber) {
        PurchaseOrderResponse response = purchaseOrderService.getCompleteRepairPartsList(user, receiptNum,vehicleNumber);
        return ApiResponse.success(SuccessStatus.SEARCH_PARTS_SUCCESS,response);
    }


    @Operation(summary = "발주 승인", description = "본사에서 발주를 승인합니다.")
    @PatchMapping("/{orderId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@CurrentUser UserContext user,@PathVariable Long orderId, @RequestBody NoteRequest request){
        purchaseOrderService.approveOrder(user, orderId, request.getNote());
        return ApiResponse.success_only(SuccessStatus.APPROVE_PURCHASE_SUCCESS);
    }

    @Operation(summary = "발주 출고일 업데이트", description = "승인된 발주 건을 출고 상태로 변경하고, 출고일(transferDate)을 기록합니다.")
    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<Void>> processShipment(@CurrentUser UserContext user, @PathVariable Long orderId){
        purchaseOrderService.ship(user, orderId);
        scheduledTaskService.scheduleNewTask(orderId);
        return ApiResponse.success_only(SuccessStatus.SHIPPED_PURCHASE_SUCCESS);
    }

    @Operation(summary = "발주 반려", description = "본사에서 발주를 반려합니다.")
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @CurrentUser UserContext user,
            @PathVariable Long orderId, @RequestBody NoteRequest request
    ){
        purchaseOrderService.rejectOrder(user, orderId, request.getNote());
        return ApiResponse.success_only(SuccessStatus.REJECT_PURCHASE_SUCCESS);
    }

}
