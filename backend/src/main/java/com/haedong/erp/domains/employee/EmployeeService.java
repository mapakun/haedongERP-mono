package com.haedong.erp.domains.employee;

import com.haedong.erp.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;

    @Transactional(readOnly = true)
    public PageResponse<EmployeeSummaryResponse> search(EmployeeSearchRequest request) {
        List<EmployeeSummaryResponse> items = employeeMapper.search(request);
        long totalCount = employeeMapper.count(request);
        return new PageResponse<>(items, totalCount, request.getPage(), request.getSize());
    }
}