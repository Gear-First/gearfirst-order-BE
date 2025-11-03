package com.gearfirst.backend.api.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderItemRequest {
    private Long partId;
    private String partName;
    private String partCode;
    private int price;
    private int quantity;

}
