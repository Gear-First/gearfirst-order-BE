package com.gearfirst.backend.api.order.infra.client;

import com.gearfirst.backend.api.order.infra.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// inventory 서비스의 주소와 prefix
@FeignClient(name = "gearfirst-inventory", url = "http://localhost:8081")
public interface InventoryClient {
    @GetMapping("/internal/inventories/{id}")
    InventoryResponse getInventoryById(@PathVariable("id") Long id);


}
