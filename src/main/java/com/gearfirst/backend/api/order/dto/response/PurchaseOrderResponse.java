package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long orderId;
    private String orderNumber;
    private String status;
    private int totalPrice;
    private LocalDateTime requestDate;
    private LocalDateTime approvedDate;
    private LocalDateTime transferDate;
    private LocalDateTime completedDate;
    private List<OrderItemResponse> items;

    public static PurchaseOrderResponse from(PurchaseOrder order, List<OrderItem> items){
        return PurchaseOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
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
