package com.gearfirst.backend.api.order.dto.response;

import lombok.Getter;

@Getter
public class CreatePurchaseOrderResponse {
    private int totalPartNum;
    private int partName;
    private int partCode;
    private int partQuantity;
    private String oderStatus;
}
