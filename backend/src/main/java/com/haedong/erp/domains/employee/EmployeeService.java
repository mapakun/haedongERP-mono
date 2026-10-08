package com.haedong.erp.domains.employee;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @Transactional(readOnly = true)
    public EmployeeDetailResponse get(Long id) {
        return employeeMapper.findDetailById(id)
                .orElseThrow(EmployeeService::notFound);
    }

    @Transactional
    public Long create(EmployeeSaveRequest request) {
        validate(request, null, request.getName());

        Employee employee = Employee.create(request);
        employeeMapper.insert(employee);
        saveDriverInfo(employee.getName(), request);

        return employee.getId();
    }

    @Transactional
    public void update(Long id, EmployeeSaveRequest request) {
        Employee employee = employeeMapper.findById(id)
                .orElseThrow(EmployeeService::notFound);
        String oldName = employee.getName();

        validate(request, id, oldName);

        employee.update(request);
        employeeMapper.update(employee);

        if (!oldName.equals(employee.getName())) {
            employeeMapper.renameDriver(oldName, employee.getName());
        }
        saveDriverInfo(employee.getName(), request);
    }

    private void validate(EmployeeSaveRequest request, Long excludeId, String excludeDriverName) {
        if (employeeMapper.existsByName(request.getName(), excludeId)) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "이미 등록된 이름입니다. 동명이인은 '홍길동1'처럼 숫자를 붙여 주세요.");
        }
        if (request.getJobType() != JobType.DRIVER && request.getSeniorityNo() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "서열은 기사만 입력할 수 있습니다.");
        }
        if (request.getSeniorityNo() != null
                && employeeMapper.existsBySeniorityNo(request.getSeniorityNo(), excludeDriverName)) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 다른 기사가 사용 중인 서열입니다.");
        }
    }

    private void saveDriverInfo(String name, EmployeeSaveRequest request) {
        if (request.getJobType() == JobType.DRIVER) {
            employeeMapper.upsertDriver(name, request.getSeniorityNo());
        } else {
            employeeMapper.deleteDriver(name);
        }
    }

    private static BusinessException notFound() {
        return new BusinessException(HttpStatus.NOT_FOUND, "직원을 찾을 수 없습니다.");
    }
}