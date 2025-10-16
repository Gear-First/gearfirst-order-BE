package com.gearfirst.backend.api.order.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VehicleResponse {
    private String vehicleNumber;
    private String vehicleModel;
    private String status;
}
