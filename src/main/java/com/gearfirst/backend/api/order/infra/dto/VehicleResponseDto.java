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
@Schema(description = "차량 정보 응답 DTO")
public class VehicleResponseDto {
    @Schema(description = "접수 번호", example = "1")
    private String repairNumber;

    @Schema(description = "차량 번호", example = "12가3456")
    private String plateNumber;

    @Schema(description = "차량 모델명", example = "쏘나타")
    private String model;

    @Schema(description = "제조사명", example = "현대")
    private String manufacturer;

    @Schema(description = "등록일", example = "2022-01-01")
    private String registeredDate;
}
