package com.gearfirst.backend.api.schedule.enums;

public enum TaskStatus {
    PENDING,    // 아직 실행 안 됨
    RUNNING,    // 실행 중
    SUCCESS,    // 성공
    FAILED,     // 실패
    RETRYING    // 재시도 중
}
