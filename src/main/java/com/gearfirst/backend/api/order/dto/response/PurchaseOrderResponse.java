package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long orderId;
    private int totalQuantity;
    private String orderStatus;
    private List<OrderItemResponse> items;

    public static PurchaseOrderResponse from(PurchaseOrder order, List<OrderItem> items) {
        return PurchaseOrderResponse.builder()
                .orderId(order.getId())
                .totalQuantity(order.getTotalQuantity())
                .orderStatus(order.getStatus().name())
                .items(items.stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
