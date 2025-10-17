package com.gearfirst.backend.api.order.infra.client;

import com.gearfirst.backend.api.order.infra.client.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.client.dto.OutboundRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// inventory 서비스의 주소와 prefix
@FeignClient(name = "gearfirst-inventory", url = "http://localhost:8081")
public interface InventoryClient {
    @GetMapping("/internal/warehouse-inventories/{id}")
    InventoryResponse getInventoryById(@PathVariable("id") Long id);

    @PostMapping("/internal/warehouse-inventories/outbounds")
    void createOutboundOrder(@RequestBody OutboundRequest request);

    @GetMapping("/internal/warehouse-inventories/carModelId}/{keyword}")
    List<InventoryResponse> getInventoriesByCarModel(@PathVariable Long carModelId, @PathVariable String keyword);

}
