package com.haedong.erp.domains.missedtrip.dto;

import com.haedong.erp.domains.missedtrip.MissedReason;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MissedTripSaveRequest {

    @NotNull(message = "결행 일자를 입력해 주세요.")
    private LocalDate missedOn;

    @NotBlank(message = "노선을 선택해 주세요.")
    @Size(max = 20, message = "노선 번호가 올바르지 않습니다.")
    private String routeNo;

    @NotBlank(message = "차량 번호를 입력해 주세요.")
    @Pattern(regexp = "^[0-9]{4}$", message = "차량 번호는 숫자 4자리로 입력해 주세요.")
    private String vehicleNo;

    @NotBlank(message = "순번을 입력해 주세요. (예: 40, 64, 85 또는 종일)")
    @Size(max = 30, message = "순번은 30자 이내로 입력해 주세요.")
    private String tripLabel;

    @Size(max = 50, message = "승무원 이름은 50자 이내로 입력해 주세요.")
    private String driverName;

    @NotNull(message = "결행 횟수를 입력해 주세요.")
    @Min(value = 0, message = "결행 횟수는 0 이상이어야 합니다.")
    @Max(value = 100, message = "결행 횟수가 너무 큽니다.")
    private Integer missedCount;

    @NotNull(message = "사유를 선택해 주세요.")
    private MissedReason reason;

    @Size(max = 200, message = "비고는 200자 이내로 입력해 주세요.")
    private String note;

    public String getRouteNo() {
        return routeNo == null ? null : routeNo.strip();
    }

    public String getVehicleNo() {
        return vehicleNo == null ? null : vehicleNo.strip();
    }

    public String getTripLabel() {
        return tripLabel == null ? null : tripLabel.strip();
    }

    /** 비어 있으면 null 로 저장한다 */
    public String getDriverName() {
        return driverName == null || driverName.isBlank() ? null : driverName.strip();
    }

    public String getNote() {
        return note == null || note.isBlank() ? null : note.strip();
    }
}