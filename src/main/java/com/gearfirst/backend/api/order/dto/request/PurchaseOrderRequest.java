package com.gearfirst.backend.api.order.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {
    private String vehicleNumber;
    private String vehicleModel;
    private Long requesterId;
    private String requesterName;
    private String requesterRole;
    private String requesterCode;
    private String receiptNum;
    private List<OrderItemRequest> items;
}
