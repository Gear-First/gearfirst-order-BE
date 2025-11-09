package com.gearfirst.backend.api.order.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseLineRequest {
    private Long productId;     //부품 아이디
    private int orderedQty;     //부품 수량
    private String lineRemark;  //비고 null
}
