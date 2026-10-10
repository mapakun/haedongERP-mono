package com.haedong.erp.domains.auth;

import com.haedong.erp.domains.employee.Employee;
import com.haedong.erp.domains.employee.Role;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String loginId;
    private final String name;
    private final Role role;
    private final boolean enabled;
    /** 로그인할 때 DB 에서 읽은 값. 이후 DB 값이 바뀌면 이 세션은 더 이상 유효하지 않다 (AuthStateCheckFilter) */
    private final OffsetDateTime authChangedAt;
    private String password;

    public LoginUser(Employee employee) {
        this.id = employee.getId();
        this.loginId = employee.getLoginId();
        this.name = employee.getName();
        this.role = employee.getRole();
        this.enabled = employee.isEnabled();
        this.authChangedAt = employee.getAuthChangedAt();
        this.password = employee.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /** 최고 관리자인지 (MASTER 보호 규칙에서 사용) */
    public boolean isMaster() {
        return role == Role.MASTER;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}