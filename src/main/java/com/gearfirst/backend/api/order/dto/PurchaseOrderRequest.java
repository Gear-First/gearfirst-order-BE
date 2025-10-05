package com.gearfirst.backend.api.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {
    private Long branchId;
    //private LocalDate requestDate;
    private List<OrderItemRequest> items;
}
