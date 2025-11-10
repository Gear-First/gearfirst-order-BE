package com.gearfirst.backend.api.order.infra.client;

import com.gearfirst.backend.api.order.infra.dto.ReceivingCreateNoteRequest;
import com.gearfirst.backend.api.order.infra.dto.WarehouseShippingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gearfirst-warehouse", url = "${warehouse.url}")
//@FeignClient(name = "gearfirst-warehouse", url = "http://localhost:8080")
public interface WarehouseClient {
    @PostMapping("/api/v1/shipping")
    void create(@RequestBody WarehouseShippingRequest request);

    @PostMapping("/api/v1/receiving")
    void create(@RequestBody ReceivingCreateNoteRequest request);


    @PostMapping("/api/v1/cancel/shipping")
    void cancel(Long orderId);

}
