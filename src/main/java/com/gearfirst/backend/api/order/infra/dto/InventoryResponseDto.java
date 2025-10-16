package com.gearfirst.backend.api.order.infra.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재고(부품) 정보 응답 DTO")
public class InventoryResponseDto {

    @Schema(description = "재고 코드", example = "1")
    private String inventoryId;

    @Schema(description = "부품명", example = "엔진오일")
    private String inventoryName;
}
