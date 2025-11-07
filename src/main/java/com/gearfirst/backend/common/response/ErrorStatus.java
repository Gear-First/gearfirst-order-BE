package com.gearfirst.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {
    /** 400 BAD_REQUEST */
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    INVALID_VEHICLE_INFO_EXCEPTION(HttpStatus.BAD_REQUEST, "입력 형식이 잘못되었습니다. 차량번호, 모델, 접수번호는 모두 입력하거나 모두 생략해야 합니다."),
    INVALID_DECISION_STATUS_EXCEPTION(HttpStatus.BAD_REQUEST,"승인 또는 반려 상태만 지정할 수 있습니다."),
    SHIPMENT_NOT_ALLOWED_EXCEPTION(HttpStatus.BAD_REQUEST, "승인되지 않은 주문에는 출고를 지시를 생성할 수 없습니다."),
    INVALID_STATUS_EXCEPTION(HttpStatus.BAD_REQUEST,"유효하지 않은 상태값입니다."),
    INVALID_USER_EXCEPTION(HttpStatus.BAD_REQUEST,"잘못된 사용자 정보 형식입니다. (Base64 디코딩 실패)"),

    /** 401 UNAUTHORIZED */
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

    /** 403 NOT_FOUND */
    NOT_ALLOW_ACCESS(HttpStatus.FORBIDDEN,"접근 권한이 없습니다."),

    /** 404 NOT_FOUND */
    NOT_FOUND_MEMBER_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 사용자 입니다."),
    NOT_FOUND_ORDER_EXCEPTION(HttpStatus.NOT_FOUND, "해당 발주를 찾을 수 없습니다.."),
    NOT_FOUND_BRANCH_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 대리점입니다."),

    /** 409 CONFLICT */
    DUPLICATE_RECEIPT_NUM_EXCEPTION(HttpStatus.CONFLICT, "이미 존재하는 수리 번호입니다."),
    ALREADY_PROCESSED_ORDER_EXCEPTION(HttpStatus.CONFLICT, "이미 처리된 발주 건 입니다."),
    INVALID_STATUS_TRANSITION_EXCEPTION(HttpStatus.CONFLICT, "현재 상태에서 요청한 상태로 전환할 수 없습니다."),
    CANCEL_NOT_ALLOWED_STATUS_EXCEPTION(HttpStatus.CONFLICT, "승인 대기 또는 승인 완료 상태의 발주만 취소할 수 있습니다."),
    NOTE_UPDATE_NOT_ALLOWED_EXCEPTION(HttpStatus.CONFLICT, "승인 대기 상태에서만 비고 수정이 가능합니다."),
    /** 500 SERVER_ERROR */
    FAIL_UPLOAD_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"파일 업로드 실패하였습니다."),
    APPROVED_ORDER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"발주 승인을 실패하였습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
