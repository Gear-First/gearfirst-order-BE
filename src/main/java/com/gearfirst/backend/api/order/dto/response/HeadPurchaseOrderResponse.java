package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HeadPurchaseOrderResponse extends PurchaseOrderResponse{
    private String branchCode;


    @Builder(builderMethodName = "hqBuilder")
    public HeadPurchaseOrderResponse(Long orderId, int totalQuantity, String orderStatus, List<OrderItemResponse> items, String branchCode) {
        super(orderId, totalQuantity, orderStatus, items);
        this.branchCode = branchCode;
    }

    public static HeadPurchaseOrderResponse from(PurchaseOrder order, List<OrderItem> items) {
        return HeadPurchaseOrderResponse.hqBuilder()
                .orderId(order.getId())
                .totalQuantity(order.getTotalQuantity())
                .orderStatus(order.getStatus().name())
                .items(items.stream().map(OrderItemResponse::from).toList())
                .branchCode(order.getBranchCode())
                .build();
    }
}
