package com.haedong.erp.domains.missedtrip.dto;

import com.haedong.erp.domains.missedtrip.MissedReason;
import com.haedong.erp.domains.missedtrip.MissedTripStatGroup;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class MissedTripStatRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private String routeNo;
    private MissedReason reason;
    private MissedTripStatGroup group;

    /** KEYWORD 기준일 때 비고에서 찾을 단어들. 쉼표로 구분 (예: "MCU, 휠모터") */
    private String keywords;

    public String getRouteNo() {
        return routeNo == null || routeNo.isBlank() ? null : routeNo.strip();
    }

    /** 쉼표로 나눈 단어 목록 (공백 제거, 빈 칸·중복 제거) */
    public List<String> getKeywordList() {
        if (keywords == null) {
            return List.of();
        }
        return Arrays.stream(keywords.split(","))
                .map(String::strip)
                .filter(word -> !word.isEmpty())
                .distinct()
                .toList();
    }
}