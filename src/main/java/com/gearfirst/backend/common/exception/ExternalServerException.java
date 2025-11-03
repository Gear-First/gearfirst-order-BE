package com.gearfirst.backend.common.exception;

import org.springframework.http.HttpStatus;

public class ExternalServerException extends BaseException {

    public ExternalServerException() {
        super(HttpStatus.BAD_GATEWAY);
    }

    public ExternalServerException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}
