package com.haedong.erp.domains.duty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.haedong.erp.domains.duty.Shift.AFTERNOON;
import static com.haedong.erp.domains.duty.Shift.MORNING;
import static com.haedong.erp.domains.duty.Shift.OFF;

/**
 * 기본 근무표의 12일 패턴 계산. 모든 전속기사가 같은 패턴을 쓰고 시작점만 다르다.
 * 1~5일차 오전, 6일차 휴무, 7~11일차 오후, 12일차 휴무
 */
public final class DutyPattern {

    public static final int CYCLE_LENGTH = 12;

    private static final List<Shift> PATTERN = List.of(
            MORNING, MORNING, MORNING, MORNING, MORNING, OFF,
            AFTERNOON, AFTERNOON, AFTERNOON, AFTERNOON, AFTERNOON, OFF
    );

    private DutyPattern() {
    }

    /** 주기 일차(1~12)의 근무 */
    public static Shift shiftOf(int cycleDay) {
        validateCycleDay(cycleDay);
        return PATTERN.get(cycleDay - 1);
    }

    /** 기준일이 cycleDay 일차일 때, date 는 몇 일차인가 (date 는 기준일 이후여야 한다) */
    public static int cycleDayOf(LocalDate baseDate, int cycleDay, LocalDate date) {
        validateCycleDay(cycleDay);
        if (date.isBefore(baseDate)) {
            throw new IllegalArgumentException("기준일 이전 날짜는 계산할 수 없습니다: " + date);
        }
        long days = ChronoUnit.DAYS.between(baseDate, date);
        return Math.floorMod(days + cycleDay - 1, CYCLE_LENGTH) + 1;
    }

    /**
     * date 에 적용되는 시작점: date 이전(같은 날 포함)을 기준일로 하는 시작점 중 가장 나중에 입력한(id 가 큰) 것.
     * → 새로 입력한 시작점은, 그 기준일 이후를 기준일로 하던 기존 시작점을 덮어쓴다.
     * 적용할 시작점이 없으면 빈 값 (= 근무표 미설정)
     */
    public static Optional<DutyStart> appliedStart(LocalDate date, List<DutyStart> starts) {
        return starts.stream()
                .filter(start -> !start.getBaseDate().isAfter(date))
                .max(Comparator.comparing(DutyStart::getId));
    }

    /** 시작점 이력으로 date 의 근무를 구한다. 적용할 시작점이 없으면 빈 값 (= 근무표 미설정) */
    public static Optional<Shift> shiftOn(LocalDate date, List<DutyStart> starts) {
        return appliedStart(date, starts)
                .map(start -> shiftOf(cycleDayOf(start.getBaseDate(), start.getCycleDay(), date)));
    }

    private static void validateCycleDay(int cycleDay) {
        if (cycleDay < 1 || cycleDay > CYCLE_LENGTH) {
            throw new IllegalArgumentException("주기 일차는 1~12 이어야 합니다: " + cycleDay);
        }
    }

    /** start 가 나중에 입력한 시작점에 완전히 덮어써졌는지: 나중 입력 중 기준일이 같거나 앞선 것이 있으면 */
    public static boolean isSuperseded(DutyStart start, List<DutyStart> starts) {
        return starts.stream()
                .anyMatch(other -> other.getId() > start.getId()
                        && !other.getBaseDate().isAfter(start.getBaseDate()));
    }

    /**
     * start 가 적용되는 마지막 날. 나중 입력 중 기준일이 더 늦은 것이 있으면 그중 가장 빠른 기준일의 전날까지.
     * 계속 적용되면 빈 값. (덮어써진 시작점에는 의미가 없다)
     */
    public static Optional<LocalDate> appliedUntil(DutyStart start, List<DutyStart> starts) {
        return starts.stream()
                .filter(other -> other.getId() > start.getId()
                        && other.getBaseDate().isAfter(start.getBaseDate()))
                .map(DutyStart::getBaseDate)
                .min(Comparator.naturalOrder())
                .map(date -> date.minusDays(1));
    }
}