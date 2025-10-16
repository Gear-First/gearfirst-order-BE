package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.PurchaseOrderResponse;
import com.gearfirst.backend.api.order.infra.dto.*;
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

//    private final PurchaseOrderService purchaseOrderService;
//
//    @Operation(summary = "엔지니어가 차량 검색", description = "대리점에서 엔지니어가 차량번호로 검색합니다.")
//    @GetMapping("/vehicles")
//    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getEngineerVehicles(
//            @RequestParam Long engineerId,
//            @RequestParam(required = false) String keyword
//    ) {
//        List<VehicleResponse> list = purchaseOrderService.findVehiclesByEngineer(engineerId, keyword);
//        return ApiResponse.success(SuccessStatus.SEARCH_VEHICLE_SUCCESS, list);
//    }
//
//    @Operation(summary = "차량에 맞는 부품 검색", description = "대리점에서 엔지니어가 부품을 검색합니다.")
//    @GetMapping("/inventories")
//    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoriesByCarModel(
//            @RequestParam Long carModelId,
//            @RequestParam(required = false) String keyword
//    ) {
//        List<InventoryResponse> list = purchaseOrderService.findInventoriesByCarModel(carModelId, keyword);
//        return ApiResponse.success(SuccessStatus.SEARCH_INVENTORY_SUCCESS, list);
//    }
//
//    @Operation(summary = "발주 요청 생성", description = "대리점이 본사로 발주 요청을 보냅니다.")
//    @PostMapping
//    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> requestPurchaseOrder(
//            @RequestBody PurchaseOrderRequest request
//    ){
//        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);
//        return ApiResponse.success(SuccessStatus.REQUEST_PURCHASE_SUCCESS, response);
//    }
//
//    @Operation(summary = "본사 발주 전체 조회", description = "대리점이 등록한 발주 내역을 조회합니다.")
//    @GetMapping("/head")
//    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getPurchaseOrders(){
//        List<PurchaseOrderResponse> list = purchaseOrderService.getAllPurchaseOrders();
//        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
//    }
//
//    @Operation(summary = "대리점 발주 전체 조회", description = "엔지니어가 자신이 등록한 발주 내역을 조회합니다.")
//    @GetMapping("/branch")
//    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getBranchPurchaseOrders(
//            @RequestParam Long branchId, @RequestParam Long engineerId
//    ){
//        List<PurchaseOrderResponse> list = purchaseOrderService.getBranchPurchaseOrders(branchId,engineerId);
//        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
//    }
//
//    @Operation(summary = "대리점 발주 상태 그룹별 조회", description = "준비 / 완료 / 취소·반려 그룹별로 발주 내역을 조회합니다.")
//    @GetMapping("/status")
//    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getBranchPurchaseOrdersByFilter(
//            @RequestParam Long branchId,  @RequestParam String filterType  // "ready", "completed", "cancelled"
//    ){
//        List<PurchaseOrderResponse> list = purchaseOrderService.getBranchPurchaseOrdersByFilter(branchId, filterType);
//        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS,list);
//    }
//
//    @Operation(summary = "본사 발주 상태별 조회", description = "승인대기, 승인완료, 반려, 출고중, 납품완료, 취소 상태별로 발주 내역을 조회합니다.")
//    @GetMapping("/head/status")
//    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getHeadPurchaseOrdersByStatus(
//            @RequestParam String status  // "PENDING", "APPROVED", "REJECTED", "SHIPPED", "COMPLETED", "CANCELLED"
//    ) {
//        List<PurchaseOrderResponse> list = purchaseOrderService.getHeadPurchaseOrdersByStatus(status);
//        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS, list);
//    }
//
//    @Operation(summary = "발주 상세 조회", description = "특정 발주 번호 상세 조회")
//    @GetMapping("/{orderId}")
//    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrderDetail(@PathVariable Long orderId){
//        PurchaseOrderResponse detail = purchaseOrderService.getPurchaseOrderDetail(orderId);
//        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,detail);
//    }
//
//    @Operation(summary = "발주 승인", description = "본사에서 발주를 승인합니다.")
//    @PatchMapping("/{orderId}/{warehouseId}/approve")
//    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long orderId, @PathVariable Long warehouseId){
//        purchaseOrderService.approveOrder(orderId, warehouseId);
//        return ApiResponse.success_only(SuccessStatus.APPROVE_PURCHASE_SUCCESS);
//    }
//    @Operation(summary = "발주 반려", description = "본사에서 발주를 반려합니다.")
//    @PatchMapping("/{orderId}/reject")
//    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long orderId){
//        purchaseOrderService.rejectOrder(orderId);
//        return ApiResponse.success_only(SuccessStatus.REJECT_PURCHASE_SUCCESS);
//    }

    @Operation(summary = "가짜 차량 목록 검색", description = "엔지니어 ID로 차량 리스트를 반환합니다 (목 데이터)")
    @GetMapping("/vehicles/all")
    public ResponseEntity<ApiResponse<List<VehicleResponseDto>>> getEngineerVehicles(
            @RequestParam Long engineerId
    ) {
        //접수 번호, 차량번호, 차종
        List<VehicleResponseDto> fakeVehicles = List.of(
                new VehicleResponseDto("RO-251011-12가","12가3456", "쏘나타",  "현대","2025-10-11"),
                new VehicleResponseDto("RO-251010-34나","34나5678", "그랜저", "현대","2025-10-09"),
                new VehicleResponseDto("RO-251001-59차","59차9218", "쏘나타", "현대","2025-10-01")
        );
        return ApiResponse.success(SuccessStatus.SEARCH_VEHICLE_SUCCESS, fakeVehicles);
    }
    @Operation(summary = "가짜 차량 목록 조회", description = "엔지니어 ID와 키워드로 검색된 차량 리스트를 반환합니다 (목 데이터)")
    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse<List<VehicleResponseDto>>> getEngineerVehicles(
            @RequestParam Long engineerId,
            @RequestParam(required = false) String keyword
    ) {
        List<VehicleResponseDto> fakeVehicles = List.of(
                new VehicleResponseDto("PO-251011-12가","12가3456", "쏘나타",  "현대","2025-10-11")
        );
        return ApiResponse.success(SuccessStatus.SEARCH_VEHICLE_SUCCESS, fakeVehicles);
    }

    @Operation(summary = "가짜 부품 검색", description = "차량 모델 ID로 검색된 부품 리스트를 반환합니다 (목 데이터)")
    @GetMapping("/inventories")
    public ResponseEntity<ApiResponse<List<InventoryResponseDto>>> getInventoriesByCarModel(
            @RequestParam Long carModelId,
            @RequestParam(required = false) String keyword
    ) {
        List<InventoryResponseDto> fakeInventories = List.of(
                new InventoryResponseDto("EF-001", "엔진 오일 필터1"),
                new InventoryResponseDto("EF-002", "엔진 오일 필터2"),
                new InventoryResponseDto("EF-003", "엔진 오일 필터3"),
                new InventoryResponseDto("EF-001", "엔진 오일 필터4")
        );
        return ApiResponse.success(SuccessStatus.SEARCH_INVENTORY_SUCCESS, fakeInventories);
    }

    @Operation(summary = "가짜 발주 요청", description = "요청 데이터를 그대로 돌려줍니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderResponseDto>> requestPurchaseOrder(
            @RequestBody PurchaseOrderRequest request
    ) {
        //  OrderItemRequest → OrderItemResponseDto 변환
        List<OrderItemResponseDto> responseItems = request.getItems().stream()
                .map(item -> new OrderItemResponseDto(
                        item.getInventoryId(),
                        item.getInventoryName(),     // 만약 요청 DTO에 name이 없다면 "임시 부품명" 등으로 채워도 됨
                        item.getQuantity()
                ))
                .toList();

        // 가짜 응답 생성
        PurchaseOrderResponseDto fakeResponse = new PurchaseOrderResponseDto(
                "P0-251016-E2",
                "PENDING",
                responseItems
        );
        return ApiResponse.success(SuccessStatus.REQUEST_PURCHASE_SUCCESS, fakeResponse);
    }

    @Operation(summary = "가짜 발주 상태별 조회", description = "필터(준비, 완료, 취소)에 따라 발주 내역을 반환합니다 (목 데이터)")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponseDto>>> getBranchPurchaseOrdersByFilter(
            @RequestParam Long branchId,
            @RequestParam String filterType
    ) {
        //발주번호, 상태, 코드 갯수 이름
        List<PurchaseOrderResponseDto> allOrders = List.of(
                new PurchaseOrderResponseDto("P0-251015-E2", "PENDING", List.of(
                        new OrderItemResponseDto(1L, "엔진오일2", 3)
                )),
                new PurchaseOrderResponseDto("P0-251003-E8", "PENDING", List.of(
                        new OrderItemResponseDto(1L, "엔진오일8", 3)
                )),
                new PurchaseOrderResponseDto("P0-251001-E1", "REJECTED", List.of(
                        new OrderItemResponseDto(1L, "엔진오일1", 3)
                )),
                new PurchaseOrderResponseDto("P0-250928-E1", "APPROVED", List.of(
                        new OrderItemResponseDto(1L, "엔진오일1", 7)
                )),
                new PurchaseOrderResponseDto("P0-250920-BP1", "SHIPPED", List.of(
                        new OrderItemResponseDto(3L, "브레이크패드1", 4)
                )),
                new PurchaseOrderResponseDto("P0-250910-BT1", "COMPLETED", List.of(
                        new OrderItemResponseDto(8L, "배터리1 (60Ah)", 1)
                )),
                new PurchaseOrderResponseDto("P0-250919-BT2", "CANCELLED", List.of(
                        new OrderItemResponseDto(10L, "배터리2 (80Ah)", 1)
                ))
        );

        List<PurchaseOrderResponseDto> filtered = allOrders.stream()
                .filter(order -> switch (filterType.toLowerCase()) {
                    case "ready" -> order.getStatus().equals("PENDING") || order.getStatus().equals("APPROVED") || order.getStatus().equals("SHIPPED") ;
                    case "completed" -> order.getStatus().equals("COMPLETED");
                    case "cancelled" -> order.getStatus().equals("REJECTED") || order.getStatus().equals("CANCELLED");
                    default -> true;
                })
                .toList();

        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS, filtered);
    }

}
