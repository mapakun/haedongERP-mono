package com.haedong.erp.domains.routeroster;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 노선 근무표 API 의 권한·입력 검증 규칙. DB 없이 컨트롤러와 보안 설정만 띄워서 확인한다 */
@WebMvcTest(RouteRosterController.class)
@Import(SecurityConfig.class)
class RouteRosterControllerSecurityTest {

    private static final String QUERY = "?year=2026&month=11&routeNo=100";

    private static final String SAVE_BODY = """
            {"rows": [{"slotNo": 1, "vehicleNo": "3115", "abLabel": "A", "employeeName": "기사"},
                      {"slotNo": 1, "vehicleNo": "3115", "abLabel": "B", "employeeName": null}]}
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RouteRosterService routeRosterService;

    @MockitoBean
    UserDetailsService userDetailsService;

    // SecurityConfig 의 AuthStateCheckFilter 가 사용
    @MockitoBean
    EmployeeMapper employeeMapper;

    @Test
    void 로그인하지_않으면_401() throws Exception {
        mockMvc.perform(get("/api/route-rosters" + QUERY)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_조회도_저장도_403() throws Exception {
        mockMvc.perform(get("/api/route-rosters" + QUERY)).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/route-rosters" + QUERY).with(csrf())
                        .contentType(APPLICATION_JSON).content(SAVE_BODY))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/route-rosters/copy-previous" + QUERY).with(csrf()))
                .andExpect(status().isForbidden());

        then(routeRosterService).should(never()).save(anyInt(), anyInt(), anyString(), any(), any());
        then(routeRosterService).should(never()).copyPrevious(anyInt(), anyInt(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_노선_근무표를_조회한다() throws Exception {
        mockMvc.perform(get("/api/route-rosters" + QUERY)).andExpect(status().isOk());

        then(routeRosterService).should().roster(2026, 11, "100");
    }

    @Test
    @WithMockUser(roles = "MASTER")
    void 최고_관리자는_배치를_저장하고_새로_만든다() throws Exception {
        mockMvc.perform(put("/api/route-rosters" + QUERY).with(csrf())
                        .contentType(APPLICATION_JSON).content(SAVE_BODY))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/route-rosters/from-vehicles" + QUERY).with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void AB_값이_잘못되면_400() throws Exception {
        String body = """
                {"rows": [{"slotNo": 1, "vehicleNo": "3115", "abLabel": "C", "employeeName": "기사"}]}
                """;

        mockMvc.perform(put("/api/route-rosters" + QUERY).with(csrf())
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        then(routeRosterService).should(never()).save(anyInt(), anyInt(), anyString(), any(), any());
    }
}
