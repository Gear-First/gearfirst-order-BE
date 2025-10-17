package com.gearfirst.backend.api.order.fixture;

import com.gearfirst.backend.api.order.dto.request.OrderItemRequest;
import com.gearfirst.backend.api.order.dto.request.PurchaseOrderRequest;
import com.gearfirst.backend.api.order.entity.PurchaseOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class PurchaseOrderFixture {
    public static PurchaseOrder createPendingOrder(Long id, Long branchId, Long engineerId) {
        PurchaseOrder order = PurchaseOrder.builder()
                .branchId(branchId)
                .engineerId(engineerId)
                .vehicleModel("쏘나타")
                .vehicleNumber("12가1234")
                .build();
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
    public static PurchaseOrder createApprovedOrder(Long id, Long branchId, Long engineerId) {
        PurchaseOrder order = createPendingOrder(id, branchId, engineerId);
        order.approve();
        return order;
    }

    public static PurchaseOrder createShippedOrder(Long id, Long branchId, Long engineerId) {
        PurchaseOrder order = createApprovedOrder(id, branchId, engineerId);
        order.ship();
        return order;
    }

    public static PurchaseOrder createCompletedOrder(Long id, Long branchId, Long engineerId) {
        PurchaseOrder order = createShippedOrder(id, branchId, engineerId);
        order.complete();
        return order;
    }
    public static PurchaseOrder createRejectedOrder(Long id, Long branchId, Long engineerId) {
        PurchaseOrder order = createPendingOrder(id, branchId, engineerId);
        order.reject();
        return order;
    }
    public static PurchaseOrder createCancelledOrder(Long id, Long branchId, Long engineerId) {
        PurchaseOrder order = createPendingOrder(id, branchId, engineerId);
        order.cancel();
        return order;
    }
}
