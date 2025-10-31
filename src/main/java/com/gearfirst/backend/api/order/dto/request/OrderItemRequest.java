package com.gearfirst.backend.api.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private Long partId;
    private String partName;
    private String partCode;
    private int price;
    private int quantity;
}
