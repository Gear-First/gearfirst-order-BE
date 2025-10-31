package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {
    private Long id;
    private String partName;
    private String partCode;
    private int price;
    private int quantity;
    private int totalPrice;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .partName(item.getPartName())
                .partCode(item.getPartCode())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
