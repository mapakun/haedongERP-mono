package com.haedong.erp.domains.missedtrip;

import com.haedong.erp.domains.missedtrip.dto.MissedTripRow;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 결행 목록을 엑셀(.xlsx) 파일로 만든다. 열 구성은 기존 엑셀(결행 목록 관리.xlsx)과 같다 */
@Component
public class MissedTripExcelWriter {

    private static final String[] HEADERS =
            {"번호", "일자", "요일", "노선", "차량번호", "순번", "승무원", "결행 횟수", "사유", "비고"};

    /** 열 너비 (글자 수 기준) */
    private static final int[] WIDTHS = {7, 12, 6, 9, 10, 12, 12, 10, 8, 50};

    private static final Map<MissedReason, String> REASON_LABEL = Map.of(
            MissedReason.TRAFFIC, "체증",
            MissedReason.BREAKDOWN, "고장",
            MissedReason.ACCIDENT, "사고",
            MissedReason.SICK, "병가",
            MissedReason.ETC, "기타");

    public byte[] write(List<MissedTripRow> rows) {
        // SXSSF 는 메모리에 일부 행만 두고 나머지는 임시 파일로 넘기는 방식이라 행이 많아도 부담이 적다
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CreationHelper helper = workbook.getCreationHelper();
            Sheet sheet = workbook.createSheet("결행 목록");

            CellStyle headerStyle = workbook.createCellStyle();
            Font bold = workbook.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(helper.createDataFormat().getFormat("yyyy-mm-dd"));
            dateStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.setFont(bold);
            totalStyle.setAlignment(HorizontalAlignment.CENTER);
            totalStyle.setBorderTop(BorderStyle.THIN);

            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, WIDTHS[i] * 256);
            }
            sheet.createFreezePane(0, 1);

            int rowIndex = 1;
            int number = 1;
            for (MissedTripRow item : rows) {
                Row row = sheet.createRow(rowIndex++);
                put(row, 0, number++, centerStyle);

                Cell date = row.createCell(1);
                date.setCellValue(item.getMissedOn());
                date.setCellStyle(dateStyle);

                put(row, 2, item.getMissedOn().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                        centerStyle);
                put(row, 3, item.getRouteNo(), centerStyle);
                put(row, 4, item.getVehicleNo(), centerStyle);
                put(row, 5, item.getTripLabel(), centerStyle);
                put(row, 6, item.getDriverName() == null ? "" : item.getDriverName(), centerStyle);
                put(row, 7, item.getMissedCount(), centerStyle);
                put(row, 8, REASON_LABEL.get(item.getReason()), centerStyle);
                row.createCell(9).setCellValue(item.getNote() == null ? "" : item.getNote());
            }

            // 맨 아래 합계 (엑셀 수식이라 나중에 줄을 지우거나 고쳐도 합계가 따라간다)
            Row total = sheet.createRow(rowIndex);
            Cell label = total.createCell(0);
            label.setCellValue("합계");
            label.setCellStyle(totalStyle);
            Cell sum = total.createCell(7);
            sum.setCellFormula(rows.isEmpty() ? "0" : "SUM(H2:H" + rowIndex + ")");
            sum.setCellStyle(totalStyle);

            workbook.write(out);
            workbook.dispose(); // 임시 파일 정리
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("엑셀 파일을 만들지 못했습니다.", e);
        }
    }

    private static void put(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void put(Row row, int column, int value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}