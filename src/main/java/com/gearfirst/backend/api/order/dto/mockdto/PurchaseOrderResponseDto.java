package com.gearfirst.backend.api.order.dto.mockdto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "발주 요청 응답 DTO")
public class PurchaseOrderResponseDto {
    @Schema(description = "발주 번호", example = "999")
    private  String repairNumber;

    @Schema(description = "발주 상태", example = "PENDING")
    private String status;

    @Schema(description = "발주 품목 리스트")
    private List<OrderItemResponseDto> items;
}
