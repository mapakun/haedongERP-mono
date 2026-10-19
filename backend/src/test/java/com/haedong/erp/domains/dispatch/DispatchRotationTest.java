package com.haedong.erp.domains.dispatch;

import com.haedong.erp.domains.dispatch.DispatchRotation.Rule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 배차순위표 회전 계산을 엑셀 순위표 값과 비교해 검증한다. DB·Spring 없이 실행된다 */
class DispatchRotationTest {

    // 엑셀 순번 1번부터 차례로 적은 차량 번호 ("0" = 빈 순번)
    private static final String[] R100_NOV_1 = {"5357", "5338", "5320", "3125", "5331", "3868", "5312", "5317",
            "4325", "5336", "5315", "3115", "5341", "5318", "5355", "5356"};
    private static final String[] R100_NOV_2 = {"5318", "5355", "5356", "5357", "5338", "5320", "3125", "5331",
            "3868", "5312", "5317", "4325", "5336", "5315", "3115", "5341"};
    private static final String[] R100_NOV_30 = {"5336", "5315", "3115", "5341", "5318", "5355", "5356", "5357",
            "5338", "5320", "3125", "5331", "3868", "5312", "5317", "4325"};

    private static final String[] R3006_OCT_1 = {"3356", "3357", "3360", "3368", "3379", "0", "0", "0", "0", "0", "0", "3355"};
    private static final String[] R3006_NOV_1 = {"3360", "3368", "3379", "0", "0", "0", "0", "0", "0", "3355", "3356", "3357"};
    private static final String[] R3006_NOV_30 = {"3357", "3360", "3368", "3379", "0", "0", "0", "0", "0", "0", "3355", "3356"};

    private static LocalDate date(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    /** 기준일 순위(엑셀 한 열)를 슬롯 → 차량 지도로 바꾼다. 순번 n 의 차량 = 슬롯 n */
    private static Map<Integer, String> slotsFrom(String[] column) {
        Map<Integer, String> slots = new HashMap<>();
        for (int i = 0; i < column.length; i++) {
            if (!"0".equals(column[i])) {
                slots.put(i + 1, column[i]);
            }
        }
        return slots;
    }

    /** 계산 결과에서 한 날짜 열을 엑셀과 같은 모양("0" = 빈 순번)으로 꺼낸다 */
    private static List<String> column(String[][] table, int dayIndex) {
        List<String> result = new ArrayList<>();
        for (String[] row : table) {
            result.add(row[dayIndex] == null ? "0" : row[dayIndex]);
        }
        return result;
    }

    @Test
    void 노선100_11월_같은_달_안에서는_일회전만큼_밀린다() {
        List<Rule> rules = List.of(new Rule(date(2026, 11, 1), 3, 6));

        String[][] table = DispatchRotation.table(date(2026, 11, 1), 16, slotsFrom(R100_NOV_1), rules,
                date(2026, 11, 1), date(2026, 11, 30));

        assertThat(column(table, 0)).containsExactly(R100_NOV_1);
        assertThat(column(table, 1)).containsExactly(R100_NOV_2);
        assertThat(column(table, 29)).containsExactly(R100_NOV_30);
    }

    @Test
    void 노선3006_빈_순번도_함께_돌고_말일에서_다음달_1일은_월회전만큼_밀린다() {
        // 10월 1일을 기준일로 두고 계산한 11월이 엑셀 11월 시트와 같아야 한다 (10월은 31일까지)
        List<Rule> rules = List.of(new Rule(date(2026, 10, 1), 5, 4));

        String[][] table = DispatchRotation.table(date(2026, 10, 1), 12, slotsFrom(R3006_OCT_1), rules,
                date(2026, 11, 1), date(2026, 11, 30));

        assertThat(column(table, 0)).containsExactly(R3006_NOV_1);
        assertThat(column(table, 29)).containsExactly(R3006_NOV_30);
    }

    @Test
    void 회전_값이_바뀌면_적용_시작일부터_새_값으로_밀린다() {
        List<Rule> rules = List.of(
                new Rule(date(2026, 11, 1), 1, 0),
                new Rule(date(2026, 11, 3), 2, 0));

        // 11/2 로 넘어갈 때 +1 (예전 값), 11/3·11/4 로 넘어갈 때 +2 씩 (새 값)
        assertThat(DispatchRotation.offsetOn(date(2026, 11, 1), date(2026, 11, 2), rules)).isEqualTo(1);
        assertThat(DispatchRotation.offsetOn(date(2026, 11, 1), date(2026, 11, 4), rules)).isEqualTo(5);
    }

    @Test
    void 이월은_실제_말일인_28일에서_월회전이_적용된다() {
        List<Rule> rules = List.of(new Rule(date(2027, 2, 1), 1, 4));

        // 2/27 → 2/28 은 일회전 +1, 2/28 → 3/1 은 월회전 +4
        assertThat(DispatchRotation.offsetOn(date(2027, 2, 27), date(2027, 2, 28), rules)).isEqualTo(1);
        assertThat(DispatchRotation.offsetOn(date(2027, 2, 27), date(2027, 3, 1), rules)).isEqualTo(5);
    }

    @Test
    void 순번은_고리처럼_끝을_넘으면_1번으로_돌아간다() {
        assertThat(DispatchRotation.orderOf(14, 16, 3)).isEqualTo(1);
        assertThat(DispatchRotation.orderOf(1, 16, 0)).isEqualTo(1);
        assertThat(DispatchRotation.orderOf(16, 16, 16)).isEqualTo(16);
    }

    @Test
    void 기준일_이전_날짜는_계산하지_않는다() {
        List<Rule> rules = List.of(new Rule(date(2026, 11, 1), 3, 6));

        assertThatThrownBy(() -> DispatchRotation.offsetOn(date(2026, 11, 1), date(2026, 10, 31), rules))
                .isInstanceOf(IllegalArgumentException.class);
    }
}