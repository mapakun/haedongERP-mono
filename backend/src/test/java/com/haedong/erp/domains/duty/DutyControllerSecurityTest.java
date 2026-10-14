package com.haedong.erp.domains.duty;

import com.haedong.erp.config.SecurityConfig;
import com.haedong.erp.domains.employee.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 근무표 API 의 권한·입력 검증 규칙. DB 없이 컨트롤러와 보안 설정만 띄워서 확인한다 */
@WebMvcTest(DutyController.class)
@Import(SecurityConfig.class)
class DutyControllerSecurityTest {

    private static final String ROSTER_URL = "/api/duty/roster?year=2026&month=10";

    private static final String STARTS_BODY = """
            {"baseDate": "2026-10-01", "items": [{"employeeName": "기사", "cycleDay": 3}]}
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DutyService dutyService;

    @MockitoBean
    UserDetailsService userDetailsService;

    // SecurityConfig 의 AuthStateCheckFilter 가 사용
    @MockitoBean
    EmployeeMapper employeeMapper;

    @Test
    void 로그인하지_않으면_근무표_401() throws Exception {
        mockMvc.perform(get(ROSTER_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_근무표_403() throws Exception {
        mockMvc.perform(get(ROSTER_URL)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/duty/starts").with(csrf()).contentType(APPLICATION_JSON).content(STARTS_BODY))
                .andExpect(status().isForbidden());

        then(dutyService).should(never()).createStarts(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_월_근무표를_조회한다() throws Exception {
        mockMvc.perform(get(ROSTER_URL)).andExpect(status().isOk());

        then(dutyService).should().roster(2026, 10);
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void 최고_관리자는_시작점을_입력한다() throws Exception {
        mockMvc.perform(post("/api/duty/starts").with(csrf()).contentType(APPLICATION_JSON).content(STARTS_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 주기_일차가_범위를_벗어나면_400() throws Exception {
        String body = """
                {"baseDate": "2026-10-01", "items": [{"employeeName": "기사", "cycleDay": 13}]}
                """;

        mockMvc.perform(post("/api/duty/starts").with(csrf()).contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        then(dutyService).should(never()).createStarts(any(), any());
    }
}