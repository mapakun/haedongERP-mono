package com.haedong.erp.domains.duty;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 12일 패턴 계산을 검증한다. DB·Spring 없이 순수 Java 로만 실행된다 */
class DutyPatternTest {

    private static LocalDate oct(int day) {
        return LocalDate.of(2026, 10, day);
    }

    @Test
    void 주기_일차별_근무는_오전5_휴무_오후5_휴무_순서다() {
        assertThat(DutyPattern.shiftOf(1)).isEqualTo(Shift.MORNING);
        assertThat(DutyPattern.shiftOf(5)).isEqualTo(Shift.MORNING);
        assertThat(DutyPattern.shiftOf(6)).isEqualTo(Shift.OFF);
        assertThat(DutyPattern.shiftOf(7)).isEqualTo(Shift.AFTERNOON);
        assertThat(DutyPattern.shiftOf(11)).isEqualTo(Shift.AFTERNOON);
        assertThat(DutyPattern.shiftOf(12)).isEqualTo(Shift.OFF);
    }

    @Test
    void 기준일로부터_일차를_계산하고_12일마다_반복한다() {
        // 10/1 = 3일차
        assertThat(DutyPattern.cycleDayOf(oct(1), 3, oct(1))).isEqualTo(3);
        assertThat(DutyPattern.cycleDayOf(oct(1), 3, oct(4))).isEqualTo(6);
        assertThat(DutyPattern.cycleDayOf(oct(1), 3, oct(13))).isEqualTo(3);   // 12일 뒤 같은 일차
        assertThat(DutyPattern.cycleDayOf(oct(1), 3, oct(31))).isEqualTo(9);
    }

    @Test
    void 월이_바뀌어도_주기는_이어진다() {
        // 10/31 = 9일차 → 11/1 = 10일차
        assertThat(DutyPattern.cycleDayOf(oct(1), 3, LocalDate.of(2026, 11, 1))).isEqualTo(10);
    }

    @Test
    void 문서_예시대로_가장_최근_시작점을_적용한다() {
        // 10/1 = 2일차, 10/5 = 10일차
        List<DutyStart> starts = List.of(
                new DutyStart(1L, "기사", oct(1), 2),
                new DutyStart(2L, "기사", oct(5), 10)
        );

        assertThat(DutyPattern.shiftOn(oct(4), starts)).contains(Shift.MORNING);    // 5일차
        assertThat(DutyPattern.shiftOn(oct(5), starts)).contains(Shift.AFTERNOON);  // 10일차
        assertThat(DutyPattern.shiftOn(oct(7), starts)).contains(Shift.OFF);        // 12일차
        assertThat(DutyPattern.shiftOn(oct(8), starts)).contains(Shift.MORNING);    // 1일차
    }

    @Test
    void 기준일이_같으면_나중에_입력한_시작점을_쓴다() {
        List<DutyStart> starts = List.of(
                new DutyStart(1L, "기사", oct(1), 1),   // 오전
                new DutyStart(2L, "기사", oct(1), 7)    // 오후 (나중에 입력)
        );

        assertThat(DutyPattern.shiftOn(oct(1), starts)).contains(Shift.AFTERNOON);
    }

    @Test
    void 시작점_이전_날짜는_미설정이다() {
        List<DutyStart> starts = List.of(new DutyStart(1L, "기사", oct(3), 1));

        assertThat(DutyPattern.shiftOn(oct(2), starts)).isEmpty();
        assertThat(DutyPattern.shiftOn(oct(2), List.of())).isEmpty();
    }

    @Test
    void 주기_일차가_범위를_벗어나면_예외다() {
        assertThatThrownBy(() -> DutyPattern.shiftOf(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DutyPattern.shiftOf(13)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 과거_날짜로_재설정하면_그_이후의_기존_시작점을_덮어쓴다() {
        List<DutyStart> starts = List.of(
                new DutyStart(1L, "기사", oct(5), 10),  // 먼저 입력
                new DutyStart(2L, "기사", oct(1), 2)    // 나중에 10/1 로 재설정
        );

        assertThat(DutyPattern.shiftOn(oct(3), starts)).contains(Shift.MORNING);    // 10/1=2 → 4일차
        assertThat(DutyPattern.shiftOn(oct(7), starts)).contains(Shift.AFTERNOON);  // 10/1=2 → 8일차 (10/5 시작점이면 12일차 휴무)
    }

    @Test
    void 나중에_입력한_미래_시작점은_그_기준일부터_적용된다() {
        List<DutyStart> starts = List.of(
                new DutyStart(1L, "기사", oct(1), 2),                     // 먼저 입력
                new DutyStart(2L, "기사", LocalDate.of(2026, 11, 1), 5)   // 나중에 11/1 계획 입력
        );

        assertThat(DutyPattern.shiftOn(oct(31), starts)).contains(Shift.AFTERNOON);                   // 10/1=2 → 8일차
        assertThat(DutyPattern.shiftOn(LocalDate.of(2026, 11, 1), starts)).contains(Shift.MORNING);  // 11/1=5 → 5일차
        assertThat(DutyPattern.shiftOn(LocalDate.of(2026, 11, 2), starts)).contains(Shift.OFF);      // 6일차
    }

    @Test
    void 나중에_입력한_미래_시작점이_있으면_그_전날까지_적용된다() {
        DutyStart oct1 = new DutyStart(1L, "기사", oct(1), 2);
        DutyStart nov1 = new DutyStart(2L, "기사", LocalDate.of(2026, 11, 1), 5);
        List<DutyStart> starts = List.of(oct1, nov1);

        assertThat(DutyPattern.isSuperseded(oct1, starts)).isFalse();
        assertThat(DutyPattern.appliedUntil(oct1, starts)).contains(oct(31));
        assertThat(DutyPattern.isSuperseded(nov1, starts)).isFalse();
        assertThat(DutyPattern.appliedUntil(nov1, starts)).isEmpty();   // 계속 적용
    }

    @Test
    void 과거로_재설정하면_그_이후의_기존_시작점은_덮어써진다() {
        DutyStart oct1 = new DutyStart(1L, "기사", oct(1), 2);
        DutyStart nov1 = new DutyStart(2L, "기사", LocalDate.of(2026, 11, 1), 5);
        DutyStart reset = new DutyStart(3L, "기사", oct(1), 7);
        List<DutyStart> starts = List.of(oct1, nov1, reset);

        assertThat(DutyPattern.isSuperseded(oct1, starts)).isTrue();    // 같은 기준일에 나중 입력
        assertThat(DutyPattern.isSuperseded(nov1, starts)).isTrue();    // 더 앞선 기준일에 나중 입력
        assertThat(DutyPattern.isSuperseded(reset, starts)).isFalse();
        assertThat(DutyPattern.appliedUntil(reset, starts)).isEmpty();
    }

    @Test
    void 날짜별로_적용되는_시작점을_찾는다() {
        DutyStart oct1 = new DutyStart(1L, "기사", oct(1), 2);
        DutyStart nov1 = new DutyStart(2L, "기사", LocalDate.of(2026, 11, 1), 5);
        List<DutyStart> starts = List.of(oct1, nov1);

        assertThat(DutyPattern.appliedStart(LocalDate.of(2026, 9, 30), starts)).isEmpty();
        assertThat(DutyPattern.appliedStart(oct(31), starts)).contains(oct1);
        assertThat(DutyPattern.appliedStart(LocalDate.of(2026, 11, 1), starts)).contains(nov1);
    }
}