package com.haedong.erp.domains.employee;

import com.haedong.erp.common.PageResponse;
import com.haedong.erp.domains.employee.dto.EmployeeDetailResponse;
import com.haedong.erp.domains.employee.dto.EmployeeSaveRequest;
import com.haedong.erp.domains.employee.dto.EmployeeSearchRequest;
import com.haedong.erp.domains.employee.dto.EmployeeSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public PageResponse<EmployeeSummaryResponse> search(@ModelAttribute EmployeeSearchRequest request) {
        return employeeService.search(request);
    }

    @GetMapping("/{id}")
    public EmployeeDetailResponse get(@PathVariable Long id) {
        return employeeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@Valid @RequestBody EmployeeSaveRequest request) {
        return Map.of("id", employeeService.create(request));
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @Valid @RequestBody EmployeeSaveRequest request) {
        employeeService.update(id, request);
    }
}