package com.haedong.erp.domains.dispatch;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 배차순위표 회전 계산. 노선의 순번은 고리 모양이고, 모든 차량이 매일 같은 칸 수만큼 뒤로 밀린다.
 * 같은 달 안에서는 일회전, 말일 → 다음 달 1일에는 월회전만큼 밀린다.
 * 기준일의 순번 = 차량의 슬롯 번호.
 */
public final class DispatchRotation {

    /** 회전 값 하나. effectiveFrom 날로 넘어갈 때부터 적용된다 */
    public record Rule(LocalDate effectiveFrom, int daily, int monthly) {
    }

    private DispatchRotation() {
    }

    /** 기준일부터 date 까지 누적 이동 칸 수 */
    public static long offsetOn(LocalDate baseDate, LocalDate date, List<Rule> rules) {
        if (date.isBefore(baseDate)) {
            throw new IllegalArgumentException("기준일 이전 날짜는 계산할 수 없습니다: " + date);
        }
        List<Rule> sorted = sortByEffectiveFrom(rules);
        long offset = 0;
        for (LocalDate day = baseDate.plusDays(1); !day.isAfter(date); day = day.plusDays(1)) {
            offset += stepInto(day, sorted);
        }
        return offset;
    }

    /** 누적 이동량이 offset 일 때 슬롯 slotNo 의 차량은 몇 순번인가 (1 ~ totalSlots) */
    public static int orderOf(int slotNo, int totalSlots, long offset) {
        return (int) Math.floorMod(slotNo - 1 + offset, (long) totalSlots) + 1;
    }

    /**
     * from ~ to 기간의 순위표. 결과[순번 - 1][날짜 순서] = 차량 번호, 빈 순번은 null
     *
     * @param vehicleBySlot 슬롯 번호 → 차량 번호 (슬롯이 비어 있는 차량은 넣지 않는다)
     */
    public static String[][] table(LocalDate baseDate, int totalSlots, Map<Integer, String> vehicleBySlot,
                                   List<Rule> rules, LocalDate from, LocalDate to) {
        for (int slotNo : vehicleBySlot.keySet()) {
            if (slotNo < 1 || slotNo > totalSlots) {
                throw new IllegalArgumentException("슬롯 번호가 순번 수를 벗어났습니다: " + slotNo);
            }
        }
        List<Rule> sorted = sortByEffectiveFrom(rules);
        int days = (int) ChronoUnit.DAYS.between(from, to) + 1;
        String[][] table = new String[totalSlots][days];

        long offset = offsetOn(baseDate, from, sorted);
        for (int i = 0; i < days; i++) {
            if (i > 0) {
                offset += stepInto(from.plusDays(i), sorted);
            }
            for (Map.Entry<Integer, String> entry : vehicleBySlot.entrySet()) {
                table[orderOf(entry.getKey(), totalSlots, offset) - 1][i] = entry.getValue();
            }
        }
        return table;
    }

    /** 전날에서 day 로 넘어갈 때 밀리는 칸 수: 1일이면 월회전, 아니면 일회전 */
    private static int stepInto(LocalDate day, List<Rule> sortedRules) {
        Rule rule = ruleOn(day, sortedRules);
        return day.getDayOfMonth() == 1 ? rule.monthly() : rule.daily();
    }

    /** day 에 유효한 회전 값: 적용 시작일이 day 이전(같은 날 포함)인 것 중 가장 최근 것 */
    private static Rule ruleOn(LocalDate day, List<Rule> sortedRules) {
        Rule found = null;
        for (Rule rule : sortedRules) {
            if (rule.effectiveFrom().isAfter(day)) {
                break;
            }
            found = rule;
        }
        if (found == null) {
            throw new IllegalArgumentException("적용할 회전 값이 없습니다: " + day);
        }
        return found;
    }

    private static List<Rule> sortByEffectiveFrom(List<Rule> rules) {
        return rules.stream().sorted(Comparator.comparing(Rule::effectiveFrom)).toList();
    }
}