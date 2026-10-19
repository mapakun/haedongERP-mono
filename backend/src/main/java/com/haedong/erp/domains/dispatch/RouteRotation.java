package com.haedong.erp.domains.dispatch;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** hd_route_rotation 한 줄 */
@Getter
@NoArgsConstructor
public class RouteRotation {

    private String routeNo;
    private LocalDate effectiveFrom;
    private int dailyRotation;
    private int monthlyRotation;
    private String createdBy;
    private OffsetDateTime createdAt;

    public DispatchRotation.Rule toRule() {
        return new DispatchRotation.Rule(effectiveFrom, dailyRotation, monthlyRotation);
    }
}