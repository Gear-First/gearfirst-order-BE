package com.gearfirst.backend.common.enums;

public enum OrderStatus {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("반려"),
    SHIPPED("출고 중"),
    COMPLETED("납품 완료");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * TODO: CANCELLED(대리점이 출고 전 발주 취소),RETURNED(대리점이 수령 후 반품)
     */
}
