package com.gearfirst.backend.api.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private Long inventoryId;
    private String inventoryName;
    private int quantity;
    private int price;
}
