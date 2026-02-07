package com.gatewayservice.service.impl;

import com.gatewayservice.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final NamedParameterJdbcTemplate _np;

    @Override
    @Transactional
    public List<String> getUserRole(Long userId) {
        String query = "SELECT * FROM pr_get_user_role(:p_user_id)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_user_id", userId);

        return _np.query(
                query,
                params,
                (rs, rowNum) -> rs.getString("role_name")
        );
    }
}
