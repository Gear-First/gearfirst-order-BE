package com.gearfirst.backend.api.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {
    private String vehicleNumber;
    private String vehicleModel;
    private Long engineerId;
    private Long branchId;
    private List<OrderItemRequest> items;
}
