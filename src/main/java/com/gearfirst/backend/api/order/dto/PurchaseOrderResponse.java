package com.gearfirst.backend.api.order.dto;

import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long orderId;
    private String branchName;
    private List<OrderItemResponse> items;
    private int totalPrice;
    private String status;
    private LocalDateTime createdAt;

//    public static PurchaseOrderResponse fromEntity(PurchaseOrder order){
//        List<OrderItemResponse> itemResponses = order.get
//    }
}
