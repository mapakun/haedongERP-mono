package com.haedong.erp.domains.auth;

import com.haedong.erp.domains.employee.Employee;
import com.haedong.erp.domains.employee.Role;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String loginId;
    private final String name;
    private final Role role;
    private final boolean enabled;
    private String password;

    public LoginUser(Employee employee) {
        this.id = employee.getId();
        this.loginId = employee.getLoginId();
        this.name = employee.getName();
        this.role = employee.getRole();
        this.enabled = employee.isEnabled();
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

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}