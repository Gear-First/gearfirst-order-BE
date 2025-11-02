package com.gearfirst.backend.api.order.dto.response;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HeadPurchaseOrderDetailResponse extends PurchaseOrderDetailResponse {
    private String branchCode;
    private String engineerName;
    private String engineerRole;
    private String note;

    public static HeadPurchaseOrderDetailResponse from(PurchaseOrder order, List<OrderItem> items) {
        return HeadPurchaseOrderDetailResponse.builder()
                // 부모 필드
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .requestDate(order.getRequestDate())
                .processedDate(order.getProcessedDate())
                .transferDate(order.getTransferDate())
                .completedDate(order.getCompletedDate())
                .items(items.stream()
                        .map(OrderItemResponse::from)
                        .collect(Collectors.toList()))
                // 자식(본사 전용) 필드
                .branchCode(order.getBranchCode())
                .engineerName(order.getEngineerName())
                .engineerRole(order.getEngineerRole())
                .note(order.getNote())
                .build();
    }

}
