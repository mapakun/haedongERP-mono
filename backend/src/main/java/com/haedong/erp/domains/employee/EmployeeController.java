package com.haedong.erp.domains.employee;

import com.haedong.erp.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public PageResponse<EmployeeSummaryResponse> search(@ModelAttribute EmployeeSearchRequest request) {
        return employeeService.search(request);
    }
}