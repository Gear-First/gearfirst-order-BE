package com.gearfirst.backend.api.order.infra.client;

import com.gearfirst.backend.api.order.infra.dto.InventoryResponse;
import com.gearfirst.backend.api.order.infra.dto.OutboundRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// inventory 서비스의 주소와 prefix
@FeignClient(name = "gearfirst-inventory", url = "http://localhost:8081")
public interface InventoryClient {
    @GetMapping("/internal/warehouse-inventories/{id}")
    InventoryResponse getInventoryById(@PathVariable("id") Long id);

    @PostMapping("/internal/outbounds")
    void createOutboundOrder(@RequestBody OutboundRequest request);

}
