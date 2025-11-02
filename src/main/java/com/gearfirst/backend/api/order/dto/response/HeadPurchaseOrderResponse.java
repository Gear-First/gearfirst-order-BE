package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HeadPurchaseOrderResponse extends PurchaseOrderDetailResponse{
    private String branchCode;
    private int totalQuantity;

    public static HeadPurchaseOrderResponse from(PurchaseOrder order, List<OrderItem> items) {
        return HeadPurchaseOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .requestDate(order.getRequestDate())
                .approvedDate(order.getApprovedDate())
                .transferDate(order.getTransferDate())
                .completedDate(order.getCompletedDate())
                .items(items.stream().map(OrderItemResponse::from).toList())
                .branchCode(order.getBranchCode())
                .totalQuantity(order.getTotalQuantity())
                .build();
    }
}
