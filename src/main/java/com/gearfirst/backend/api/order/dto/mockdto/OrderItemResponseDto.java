package com.gearfirst.backend.api.order.dto.mockdto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "발주 품목 응답 DTO")
public class OrderItemResponseDto {
    @Schema(description = "부품 ID", example = "1")
    private Long inventoryId;

    @Schema(description = "부품명", example = "엔진오일")
    private String name;

    @Schema(description = "요청 수량", example = "5")
    private int quantity;
}
