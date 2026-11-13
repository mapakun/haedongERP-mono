package com.haedong.erp.domains.comfortleave;

import com.haedong.erp.common.BusinessException;
import com.haedong.erp.domains.auth.LoginUser;
import com.haedong.erp.domains.comfortleave.dto.ComfortContractRow;
import com.haedong.erp.domains.comfortleave.dto.ComfortLeaveResponse;
import com.haedong.erp.domains.comfortleave.dto.ComfortMemberRow;
import com.haedong.erp.domains.comfortleave.dto.ComfortUseRow;
import com.haedong.erp.domains.comfortleave.dto.ContractCreateRequest;
import com.haedong.erp.domains.comfortleave.dto.ContractLeaveRow;
import com.haedong.erp.domains.comfortleave.dto.DriverMemberRow;
import com.haedong.erp.domains.comfortleave.dto.MemberSaveRequest;
import com.haedong.erp.domains.comfortleave.dto.MonthlySummaryRow;
import com.haedong.erp.domains.comfortleave.dto.RegularLeaveRow;
import com.haedong.erp.domains.comfortleave.dto.UseCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComfortLeaveService {

    /** 정규직이 해마다 받는 위로휴가 개수 */
    static final int REGULAR_GRANT = 4;

    private final ComfortLeaveMapper comfortLeaveMapper;

    // ===================== 현황 조회 =====================

    @Transactional(readOnly = true)
    public ComfortLeaveResponse findYear(int year) {
        LocalDate today = LocalDate.now();
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        Map<Long, ComfortMemberRow> members = comfortLeaveMapper.findMembers().stream()
                .collect(Collectors.toMap(ComfortMemberRow::getEmployeeId, Function.identity()));

        // 그해와 기간이 겹치는 촉탁 계약 (대상자인 사람만)
        List<ComfortContractRow> contracts = comfortLeaveMapper.findContracts().stream()
                .filter(c -> members.containsKey(c.getEmployeeId()))
                .filter(c -> !c.getStartDate().isAfter(yearEnd) && !c.getEndDate().isBefore(yearStart))
                .toList();

        // 계약이 해를 넘어가면 그 앞뒤 사용 기록도 필요하므로 조회 범위를 넓힌다
        LocalDate from = contracts.stream().map(ComfortContractRow::getStartDate)
                .filter(d -> d.isBefore(yearStart)).min(Comparator.naturalOrder()).orElse(yearStart);
        LocalDate to = contracts.stream().map(ComfortContractRow::getEndDate)
                .filter(d -> d.isAfter(yearEnd)).max(Comparator.naturalOrder()).orElse(yearEnd);
        Map<Long, List<ComfortUseRow>> usesByEmployee = comfortLeaveMapper.findUsesBetween(from, to).stream()
                .collect(Collectors.groupingBy(ComfortUseRow::getEmployeeId));

        List<RegularLeaveRow> regulars = new ArrayList<>();
        for (ComfortMemberRow member : members.values()) {
            if (member.getEmploymentType() != EmploymentType.REGULAR) {
                continue;
            }
            if (member.getRetiredAt() != null && member.getRetiredAt().isBefore(yearStart)) {
                continue; // 그해가 시작되기 전에 퇴사한 사람
            }
            List<ComfortUseRow> uses = usesBetween(usesByEmployee, member.getEmployeeId(), yearStart, yearEnd);
            // 입사한 해는 0. 입사일자를 모르면 이미 근무 중인 것으로 보고 4개
            boolean hiredBeforeYear = member.getHireDate() == null || member.getHireDate().isBefore(yearStart);
            int granted = hiredBeforeYear ? REGULAR_GRANT : 0;
            regulars.add(new RegularLeaveRow(member.getEmployeeId(), member.getName(), member.getHireDate(),
                    member.getRetiredAt(), granted, uses.size(), granted - uses.size(), uses));
        }
        regulars.sort(Comparator.comparing(RegularLeaveRow::name));

        List<ContractLeaveRow> contractRows = new ArrayList<>();
        for (ComfortContractRow contract : contracts) {
            ComfortMemberRow member = members.get(contract.getEmployeeId());
            List<ComfortUseRow> uses = usesBetween(usesByEmployee, contract.getEmployeeId(),
                    contract.getStartDate(), contract.getEndDate());
            int granted = contractGranted(contract.getStartDate(), contract.getEndDate(), today);
            contractRows.add(new ContractLeaveRow(member.getEmployeeId(), member.getName(),
                    contract.getStartDate(), contract.getEndDate(), member.getRetiredAt(),
                    granted, uses.size(), granted - uses.size(), uses));
        }

        return new ComfortLeaveResponse(year, today, regulars, contractRows, monthlySummary(year, today, regulars));
    }

    /**
     * 촉탁직 발생 개수 (엑셀 수식과 같은 규칙).
     * 기준일 = 오늘과 만료일 중 빠른 날. 시작 후 3개월 전 0, 6개월 전 1, 9개월 전 2, 그 뒤 4
     */
    static int contractGranted(LocalDate start, LocalDate end, LocalDate today) {
        LocalDate asOf = today.isAfter(end) ? end : today;
        if (asOf.isBefore(start.plusMonths(3))) {
            return 0; // 시작 전 계약도 여기서 0
        }
        if (asOf.isBefore(start.plusMonths(6))) {
            return 1;
        }
        if (asOf.isBefore(start.plusMonths(9))) {
            return 2;
        }
        return 4;
    }

    /** 정규직의 월말 누계: 그달 말일까지 첫 번째~네 번째 사용이 몇 건인지 */
    private List<MonthlySummaryRow> monthlySummary(int year, LocalDate today, List<RegularLeaveRow> regulars) {
        int lastMonth = year < today.getYear() ? 12 : (year == today.getYear() ? today.getMonthValue() : 0);
        List<MonthlySummaryRow> rows = new ArrayList<>();
        for (int month = 1; month <= lastMonth; month++) {
            LocalDate monthEnd = YearMonth.of(year, month).atEndOfMonth();
            int[] byOrder = new int[REGULAR_GRANT];
            for (RegularLeaveRow regular : regulars) {
                long usedSoFar = regular.uses().stream().filter(u -> !u.getUsedOn().isAfter(monthEnd)).count();
                for (int order = 0; order < REGULAR_GRANT; order++) {
                    if (usedSoFar > order) {
                        byOrder[order]++;
                    }
                }
            }
            rows.add(new MonthlySummaryRow(month, byOrder[0], byOrder[1], byOrder[2], byOrder[3],
                    byOrder[0] + byOrder[1] + byOrder[2] + byOrder[3]));
        }
        return rows;
    }

    private List<ComfortUseRow> usesBetween(Map<Long, List<ComfortUseRow>> usesByEmployee, Long employeeId,
                                            LocalDate from, LocalDate to) {
        return usesByEmployee.getOrDefault(employeeId, List.of()).stream()
                .filter(u -> !u.getUsedOn().isBefore(from) && !u.getUsedOn().isAfter(to))
                .toList();
    }

    // ===================== 사용 기록 =====================

    /** 미리 사용도 허용한다 (잔여가 음수가 될 수 있음). 촉탁직은 사용일이 계약 기간 안이어야 한다 */
    @Transactional
    public void addUse(UseCreateRequest request, LoginUser currentUser) {
        EmploymentType type = lockMemberType(request.employeeId());
        LocalDate usedOn = request.usedOn();

        if (type == EmploymentType.CONTRACT) {
            boolean inContract = comfortLeaveMapper.findContractsOf(request.employeeId()).stream()
                    .anyMatch(c -> !usedOn.isBefore(c.getStartDate()) && !usedOn.isAfter(c.getEndDate()));
            if (!inContract) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "촉탁직은 사용일이 계약 기간 안에 있어야 합니다. 계약을 먼저 등록해 주세요.");
            }
        }
        comfortLeaveMapper.insertUse(request.employeeId(), usedOn, blankToNull(request.memo()),
                currentUser.getName());
    }

    @Transactional
    public void deleteUse(Long id) {
        if (comfortLeaveMapper.deleteUse(id) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "사용 기록을 찾을 수 없습니다.");
        }
    }

    // ===================== 대상자 관리 =====================

    @Transactional(readOnly = true)
    public List<DriverMemberRow> findDriverMembers() {
        return comfortLeaveMapper.findDriverMembers();
    }

    @Transactional
    public void saveMember(Long employeeId, MemberSaveRequest request) {
        comfortLeaveMapper.lockDriverName(employeeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "운전직 직원을 찾을 수 없습니다."));
        comfortLeaveMapper.upsertMember(employeeId, request.employmentType(), request.hireDate(),
                blankToNull(request.note()));
    }

    // ===================== 촉탁 계약 =====================
    @Transactional(readOnly = true)
    public List<ComfortContractRow> findContractsOf(Long employeeId) {
        return comfortLeaveMapper.findContractsOf(employeeId);
    }

    /** 만료일을 비우면 시작일 + 1년 - 1일. 같은 사람의 계약끼리 기간이 겹치면 안 된다 */
    @Transactional
    public void addContract(Long employeeId, ContractCreateRequest request) {
        if (lockMemberType(employeeId) != EmploymentType.CONTRACT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "촉탁직 대상자만 계약을 추가할 수 있습니다.");
        }
        LocalDate start = request.startDate();
        LocalDate end = request.endDate() != null ? request.endDate() : start.plusYears(1).minusDays(1);
        if (end.isBefore(start)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "만료일은 시작일보다 빠를 수 없습니다.");
        }

        for (ComfortContractRow existing : comfortLeaveMapper.findContractsOf(employeeId)) {
            if (!start.isAfter(existing.getEndDate()) && !end.isBefore(existing.getStartDate())) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "기존 계약(" + existing.getStartDate() + " ~ " + existing.getEndDate() + ")과 기간이 겹칩니다.");
            }
        }
        comfortLeaveMapper.insertContract(employeeId, start, end);
    }

    /** 계약 기간에 사용 기록이 있으면 삭제하지 않는다 (기록이 어느 계약에도 속하지 않게 되므로) */
    @Transactional
    public void deleteContract(Long employeeId, LocalDate startDate) {
        lockMemberType(employeeId);
        ComfortContractRow contract = comfortLeaveMapper.findContractsOf(employeeId).stream()
                .filter(c -> c.getStartDate().equals(startDate))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "계약을 찾을 수 없습니다."));

        int uses = comfortLeaveMapper.countUsesBetween(employeeId, contract.getStartDate(), contract.getEndDate());
        if (uses > 0) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "이 계약 기간에 사용 기록이 " + uses + "건 있어 삭제할 수 없습니다. 사용 기록을 먼저 삭제해 주세요.");
        }
        comfortLeaveMapper.deleteContract(employeeId, startDate);
    }

    // ===================== 공통 =====================

    /** 대상자 행을 잠그고 고용 형태를 돌려준다. 같은 사람에 대한 기록·계약 변경이 겹치지 않게 한다 */
    private EmploymentType lockMemberType(Long employeeId) {
        return comfortLeaveMapper.lockMemberType(employeeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "위로휴가 대상자가 아닙니다. 대상자 관리에서 먼저 등록해 주세요."));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}