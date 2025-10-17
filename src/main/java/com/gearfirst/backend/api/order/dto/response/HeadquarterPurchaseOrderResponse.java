package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HeadquarterPurchaseOrderResponse extends PurchaseOrderResponse{
    private String engineerName;
    private String branchName;

    public static HeadquarterPurchaseOrderResponse from(
            PurchaseOrder order,
            List<OrderItem> items,
            String engineerName,
            String branchName
    ) {
        return HeadquarterPurchaseOrderResponse.builder()
                // 부모 필드
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .requestDate(order.getRequestDate())
                .approvedDate(order.getApprovedDate())
                .transferDate(order.getTransferDate())
                .completedDate(order.getCompletedDate())
                .items(items.stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                // 자식 필드
                .engineerName(engineerName)
                .branchName(branchName)
                .build();
    }
}
