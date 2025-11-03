package com.gearfirst.backend.api.order.infra.client;

import com.gearfirst.backend.api.order.infra.dto.WarehouseShippingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gearfirst-warehouse", url = "http://localhost:8081")
public interface WarehouseClient {
    @PostMapping("/api/v1/shipping")
    void create(@RequestBody WarehouseShippingRequest request);

    @PostMapping("/api/v1/cancel/shipping")
    void cancel(Long orderId);

}
