package com.gearfirst.backend.api.order.fixture;

import com.gearfirst.backend.api.order.infra.dto.ReceiptCarResponse;

import java.util.List;

public class ReceiptCarFixture {
    public static List<ReceiptCarResponse> createFakeRepairs() {
        return List.of(
                new ReceiptCarResponse("RO-123", "98가1234", "쏘나타", "RECEIPT"),
                new ReceiptCarResponse("RO-456", "98가5421", "아반떼", "RECEIPT"),
                new ReceiptCarResponse("RO-789", "98타1234", "제네시스", "REPAIRING"),
                new ReceiptCarResponse("RO-790", "86가1234", "그랜저", "REPAIRING")
        );
    }
}
