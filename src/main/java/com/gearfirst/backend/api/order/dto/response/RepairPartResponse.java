package com.gearfirst.backend.api.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepairPartResponse {
    private String partName;
    private String partCode;
    private int quantity;
    private int price;
}
