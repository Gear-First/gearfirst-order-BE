package com.gearfirst.backend.common.enums;

public enum OrderStatus {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("반려"),
    SHIPPED("출고 중"),
    COMPLETED("납품 완료"),
    CANCELLED("취소"),
    USED_IN_REPAIR("수리에 사용 됨");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * TODO: RETURNED(대리점이 수령 후 반품)
     */
}
