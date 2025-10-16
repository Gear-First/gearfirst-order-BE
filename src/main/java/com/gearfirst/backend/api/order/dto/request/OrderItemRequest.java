package com.gearfirst.backend.api.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private Long inventoryId;
    private String inventoryName;
    private String inventoryCode;
    private int price;
    private int quantity;
}
