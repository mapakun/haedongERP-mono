package com.haedong.erp.domains.employee.dto;

import com.haedong.erp.common.NameNormalizer;
import com.haedong.erp.domains.employee.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeSaveRequest {

    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.")
    private String name;

    @NotNull(message = "직종을 선택해 주세요.")
    private JobType jobType;

    @Past(message = "생년월일이 올바르지 않습니다.")
    private LocalDate birthDate;

    @Pattern(regexp = "^(01\\d-?\\d{3,4}-?\\d{4})?$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String mobile;

    @Positive(message = "서열은 1 이상이어야 합니다.")
    private Integer seniorityNo;

    public String getName() {
        return NameNormalizer.normalize(name);
    }

    public String getMobile() {
        return mobile == null || mobile.isBlank() ? null : mobile.replaceAll("\\D", "");
    }
}