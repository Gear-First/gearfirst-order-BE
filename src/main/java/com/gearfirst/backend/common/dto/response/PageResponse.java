package com.gearfirst.backend.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private final List<T> content;          //현재 페이지의 실제 데이터 목록
    private final int pageNumber;           //현재 페이지 번호
    private final int pageSize;             //한 페이지 크기
    private final long totalElements;       //전체 데이터 개수
    private final int totalPages;           //전체 페이지 개수
    private final boolean last;             //마지막 페이지인지 여부
    private final Sort sort;               //정렬 정보

    public PageResponse(Page<T> page){
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.sort = page.getSort();
    }
}
