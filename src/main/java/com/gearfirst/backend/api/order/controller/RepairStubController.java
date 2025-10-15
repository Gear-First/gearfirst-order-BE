package com.gearfirst.backend.api.order.controller;

import com.gearfirst.backend.api.order.dto.RepairResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.gearfirst.backend.common.enums.OrderStatus.PENDING;

@RestController
@RequestMapping("/api/v1/repairs")
public class RepairStubController {
    @GetMapping("/{engineerId}/{keyword}")
    public List<RepairResponse> getRepairsByEngineer(
            @PathVariable Long engineerId,
            @PathVariable String keyword
    ) {
        return List.of(
                new RepairResponse("12가1234", "소나타", "소나타")
        );
    }
}
