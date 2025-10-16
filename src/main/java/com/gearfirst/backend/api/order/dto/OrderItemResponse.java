package com.gearfirst.backend.api.order.dto;

import com.gearfirst.backend.api.order.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {
    private Long id;
    private String inventoryName;
    private String inventoryCode;
    private int price;
    private int quantity;
    private int totalPrice;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .inventoryName(item.getInventoryName())
                .inventoryCode(item.getInventoryCode())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
