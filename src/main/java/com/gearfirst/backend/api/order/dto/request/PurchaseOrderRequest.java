package com.gearfirst.backend.api.order.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {
    private String vehicleNumber;
    private String vehicleModel;
    private Long engineerId;
    private String branchCode;
    private String receiptNum;
    private List<OrderItemRequest> items;
}
