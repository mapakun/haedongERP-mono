package com.haedong.erp.domains.employee;

import com.haedong.erp.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 직원 API 의 권한 규칙을 검증한다. DB 없이 컨트롤러와 보안 설정만 띄워서 확인한다.
 */
@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerSecurityTest {

    private static final String BODY = """
            {"name": "테스트", "jobType": "DRIVER"}
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EmployeeService employeeService;

    @MockitoBean
    UserDetailsService userDetailsService;

    // SecurityConfig 의 AuthStateCheckFilter 가 사용 (테스트의 가짜 로그인 사용자는 검사 대상이 아님)
    @MockitoBean
    EmployeeMapper employeeMapper;

    @Test
    void 로그인하지_않으면_등록_401() throws Exception {
        mockMvc.perform(post("/api/employees").with(csrf()).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_등록_403() throws Exception {
        mockMvc.perform(post("/api/employees").with(csrf()).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_수정_403() throws Exception {
        mockMvc.perform(put("/api/employees/1").with(csrf()).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_목록만_가능하고_상세는_403() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());
        mockMvc.perform(get("/api/employees/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_간략_목록을_받는다() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());

        then(employeeService).should().searchBrief(any());
        then(employeeService).should(never()).search(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_전체_목록을_받는다() throws Exception {
        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());

        then(employeeService).should().search(any());
        then(employeeService).should(never()).searchBrief(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_계정_관리_403() throws Exception {
        mockMvc.perform(post("/api/employees/1/account").with(csrf())
                        .contentType(APPLICATION_JSON).content("""
                                {"password": "password1234", "role": "ADMIN"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/employees/1/account").with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/employees/1/account/role").with(csrf())
                        .contentType(APPLICATION_JSON).content("""
                                {"role": "ADMIN"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 관리자는_등록_가능() throws Exception {
        given(employeeService.create(any())).willReturn(1L);

        mockMvc.perform(post("/api/employees").with(csrf()).contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void CSRF_토큰이_없으면_관리자도_403() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 일반_사용자는_퇴사_처리_403() throws Exception {
        mockMvc.perform(post("/api/employees/1/retire").with(csrf())
                        .contentType(APPLICATION_JSON).content("""
                                {"retiredAt": "2026-01-01"}
                                """))
                .andExpect(status().isForbidden());
    }
}
