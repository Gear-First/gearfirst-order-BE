package com.gearfirst.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN); // 403 상태 코드
    }

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
