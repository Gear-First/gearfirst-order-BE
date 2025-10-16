package com.gearfirst.backend.api.order.infra.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class InventoryResponse {
    private Long inventoryId;
    private String inventoryName;
    private String inventoryCode;
    private int price;
}
