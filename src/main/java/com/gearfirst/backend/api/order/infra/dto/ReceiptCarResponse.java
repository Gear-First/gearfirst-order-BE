package com.gearfirst.backend.api.order.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReceiptCarResponse {
    private String receiptNumber;
    private String vehicleNumber;
    private String vehicleModel;
    private String status;
}
