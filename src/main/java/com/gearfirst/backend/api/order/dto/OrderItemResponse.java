package com.gearfirst.backend.api.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemResponse {
    private String inventoryName;
    private int quantity;
    private int price;
    private int totalPrice;
}
