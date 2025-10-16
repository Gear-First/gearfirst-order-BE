package com.gearfirst.backend.api.order.infra.client;

import com.gearfirst.backend.api.order.dto.RepairResponse;
import com.gearfirst.backend.api.order.infra.dto.VehicleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "gearfirst-repair", url = "http://localhost:8082")
public interface RepairClient {
    @GetMapping("/api/v1/repairs/{engineerId}/{keyword}")
    List<RepairResponse> getRepairsByEngineer(@PathVariable Long engineerId, @PathVariable String keyword);

}
