package com.gearfirst.backend.api.order.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 도메인 내부 비지니스 명령(출고 명령)
 */
@Getter
@AllArgsConstructor
public class ShipmentCommand {
    private Long orderId;
    private String branchCode;
    private String warehouseCode;
}
