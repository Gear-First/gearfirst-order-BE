package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.infra.client.dto.ReceiptCarResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repairs")
public class RepairStubController {
    @GetMapping("/{engineerId}")
    public List<ReceiptCarResponse> getAllRepairsByEngineer(
            @PathVariable Long engineerId
    ) {
        return List.of(
                new ReceiptCarResponse("RO-251015-ER","12가1234", "소나타", "PENDING"),
                new ReceiptCarResponse("RO-251014-BR","73아5032", "아반떼", "APPROVED"),
                new ReceiptCarResponse("RO-251002-WE","95파2342", "그랜저", "SHIPPED"),
                new ReceiptCarResponse("RO-250915-BT","23하5293", "소나타", "COMPLETED")
        );
    }

    @GetMapping("/{engineerId}/{keyword}")
    public List<ReceiptCarResponse> getRepairsByEngineer(
            @PathVariable Long engineerId,
            @PathVariable String keyword
    ) {
        return List.of(
                new ReceiptCarResponse("RO-251015-ER","12가1234", "소나타", "PENDING")
        );
    }
}
