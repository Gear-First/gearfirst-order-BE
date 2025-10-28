package com.gearfirst.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {
    /** 200 SUCCESS */
    SEND_SAMPLE_SUCCESS(HttpStatus.OK,"샘플 조회 성공"),
    SEND_PURCHASE_LIST_SUCCESS(HttpStatus.OK, "발주 목록 조회 성공."),
    SEND_PURCHASE_FILTER_LIST_SUCCESS(HttpStatus.OK, "발주 필터 목록 조회 성공."),
    SEND_PURCHASE_DETAIL_SUCCESS(HttpStatus.OK, "발주 상세정보 조회 성공."),
    SEARCH_VEHICLE_SUCCESS(HttpStatus.OK, "차량 번호로 검색 성공"),
    SEARCH_INVENTORY_SUCCESS(HttpStatus.OK,"차량에 맞는 부품 검색 성공"),
    SEARCH_PARTS_SUCCESS(HttpStatus.OK,"수리 완료 시 발주 부품 내역 조회 성공"),

    /** 201 CREATED */
    CREATE_SAMPLE_SUCCESS(HttpStatus.CREATED, "샘플 등록 성공"),
    REQUEST_PURCHASE_SUCCESS(HttpStatus.CREATED, "발주 요청이 성공적으로 접수되었습니다."),
    APPROVE_PURCHASE_SUCCESS(HttpStatus.CREATED, "발주가 승인되었습니다."),
    CANCEL_PURCHASE_SUCCESS(HttpStatus.CREATED, "발주가 취소되었습니다."),
    REJECT_PURCHASE_SUCCESS(HttpStatus.CREATED, "발주가 반려되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
