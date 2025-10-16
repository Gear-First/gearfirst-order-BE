package com.gearfirst.backend.api.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReceiptResponse {
    private String vehicleNumber;
    private String vehicleModel;
    private String status;
}
