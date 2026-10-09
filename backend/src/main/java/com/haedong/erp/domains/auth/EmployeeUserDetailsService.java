package com.haedong.erp.domains.auth;

import com.haedong.erp.common.NameNormalizer;
import com.haedong.erp.domains.employee.Employee;
import com.haedong.erp.domains.employee.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String loginId = NameNormalizer.normalize(username);

        Employee employee = employeeMapper.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정: " + loginId));

        return new LoginUser(employee);
    }
}
