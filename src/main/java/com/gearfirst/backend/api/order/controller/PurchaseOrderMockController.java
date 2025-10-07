package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.OrderItemResponse;
import com.gearfirst.backend.api.order.dto.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.dto.PurchaseOrderResponse;
import com.gearfirst.backend.common.response.ApiResponse;
import com.gearfirst.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders/mock")
@Tag(name = "Mock Purchase Order API", description = "대리점 발주 요청/조회 Mock API")
public class PurchaseOrderMockController {
    /**
     * 발주 요청 생성
     */
    @Operation(summary = "발주 요청 생성", description = "대리점이 부품 발주 요청을 제출합니다. (Mock API)")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> requestPurchaseOrder(@RequestBody PurchaseOrderRequest request){
        int total = request.getItems().stream()
                .mapToInt(i-> i.getPrice() * i.getQuantity())
                .sum();
        PurchaseOrderResponse mockResponse = new PurchaseOrderResponse(
                1001L,
                "서울 강남 대리점",
                List.of(
                        new OrderItemResponse("에어필터", 5, 15000, 75000),
                        new OrderItemResponse("브레이크 패드", 2, 30000, 60000)
                ),
                total,
                "PENDING",
                LocalDateTime.now()
        );
        return ApiResponse.success(SuccessStatus.REQUEST_PURCHASE_SUCCESS, mockResponse);
    }
    /**
     * 발주 목록 조회 (Mock)
     */
    @Operation(summary = "발주 목록 조회", description = "대리점이 자신이 등록한 발주 내역 목록을 조회합니다. (Mock API)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getPurchaseOrders(
            @RequestParam Long branchId,
            @RequestParam(required = false) String status
    ){
        List<PurchaseOrderResponse> mockList = List.of(
                new PurchaseOrderResponse(1001L, "서울 강남 대리점",
                        List.of(new OrderItemResponse("에어필터", 5, 15000, 75000)),
                        75000, "PENDING", LocalDateTime.now().minusDays(1)),

                new PurchaseOrderResponse(1002L, "서울 강남 대리점",
                        List.of(new OrderItemResponse("브레이크 패드", 2, 30000, 60000)),
                        60000, "APPROVED", LocalDateTime.now().minusDays(2))
        );
        List<PurchaseOrderResponse> filteredList = (status != null && !status.isBlank())
                ? mockList.stream()
                .filter(order -> order.getStatus().equalsIgnoreCase(status))
                .toList()
                : mockList; // status가 없으면 전체 반환

        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_LIST_SUCCESS, mockList);
    }
    /**
     * 발주 상세 조회 (Mock)
     */
    @Operation(summary = "발주 상세 조회", description = "특정 발주 번호에 대한 상세 정보를 조회합니다. (Mock API)")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getPurchaseOrderDetail(@PathVariable Long orderId){
        PurchaseOrderResponse mockResponse = new PurchaseOrderResponse(
                orderId,
                "서울 강남 대리점",
                List.of(
                        new OrderItemResponse("에어필터", 5, 15000, 75000),
                        new OrderItemResponse("브레이크 패드", 2, 30000, 60000)
                ),
                135000,
                "PENDING",
                LocalDateTime.now().minusDays(1)
        );
        return ApiResponse.success(SuccessStatus.SEND_PURCHASE_DETAIL_SUCCESS,mockResponse);
    }
}
