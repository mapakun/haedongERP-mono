package com.haedong.erp.domains.dispatch;

import com.haedong.erp.domains.dispatch.dto.DispatchAllResponse;
import com.haedong.erp.domains.dispatch.dto.DispatchTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    /** 전체 노선 통합 순위표 */
    @GetMapping
    public DispatchAllResponse allTables(@RequestParam int year, @RequestParam int month) {
        return dispatchService.allTables(year, month);
    }

    /** 노선별 월 순위표 */
    @GetMapping("/routes/{routeNo}")
    public DispatchTableResponse routeTable(@PathVariable String routeNo,
                                            @RequestParam int year,
                                            @RequestParam int month) {
        return dispatchService.routeTable(routeNo, year, month);
    }
}