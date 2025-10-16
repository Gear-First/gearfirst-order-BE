package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PurchaseOrderResponse {
    private Long id;
    private String orderNumber;
    private Long engineerId;
    private Long branchId;
    private String status;
    private int totalPrice;
    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
    private LocalDateTime transferDate;
    private LocalDateTime completedDate;
    private List<OrderItemResponse> items;

    public static PurchaseOrderResponse from(PurchaseOrder order, List<OrderItem> items){
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .engineerId(order.getEngineerId())
                .branchId(order.getBranchId())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .requestDate(order.getRequestDate())
                .approvedDate(order.getApprovedDate())
                .transferDate(order.getTransferDate())
                .completedDate(order.getCompletedDate())
                .items(items.stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
