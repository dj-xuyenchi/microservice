package com.gatewayservice.service.impl;

import com.gatewayservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final NamedParameterJdbcTemplate _np;

    @Override
    @Transactional
    public List<String> getUserRole(String userName) {
        log.info("//getUserRole -> {}", userName);
        String query = "SELECT * FROM public.fn_get_user_role(:p_user_name)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_user_name", userName);

        return _np.query(
                query,
                params,
                (rs, rowNum) -> rs.getString("role_code")
        );
    }
}
