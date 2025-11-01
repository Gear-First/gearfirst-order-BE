package com.gearfirst.backend.api.order.infra.client.dto;

import com.gearfirst.backend.api.order.entity.OrderItem;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
/**
 * 출고 명령 생성 요청 DTO
 * - Order 서비스 → Inventory 서비스 통신용
 * - 본사 승인 시 사용됨
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutboundRequest {
    private Long purchaseOrderId;   //발주서 id
    private Long warehouseId;       //출고할 창고 id
    private List<Item> items;        //출고 품목 리스트


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long inventoryId;  // 부품 ID
        private int quantity;      // 출고 수량
    }

    public static OutboundRequest from(PurchaseOrder order, List<OrderItem> orderItems,Long warehouseId){
        return new OutboundRequest(
                order.getId(),
                warehouseId,
                orderItems.stream()
                        .map(i-> new Item(i.getPartId(), i.getQuantity()))
                        .toList()
        );
    }
}
