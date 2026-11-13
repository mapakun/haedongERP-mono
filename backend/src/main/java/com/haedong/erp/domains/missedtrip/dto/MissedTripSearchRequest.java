package com.haedong.erp.domains.missedtrip.dto;

import com.haedong.erp.domains.missedtrip.MissedReason;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class MissedTripSearchRequest {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    private String routeNo;
    private MissedReason reason;
    /** 승무원·차량번호·비고에서 찾는 글자 */
    private String keyword;
    private Integer page;
    private Integer size;

    public int getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int getSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public int getOffset() {
        return (getPage() - 1) * getSize();
    }

    public String getRouteNo() {
        return routeNo == null || routeNo.isBlank() ? null : routeNo.strip();
    }

    public String getKeyword() {
        return keyword == null || keyword.isBlank() ? null : keyword.strip();
    }
}